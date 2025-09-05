package com.loopers.application.payment;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.stock.StockApplicationEventPublisher;
import com.loopers.application.stock.StockChanged;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PgPaymentInfo;
import com.loopers.domain.payment.port.PaymentGatewayPort;
import com.loopers.domain.payment.result.TransactionStatusResult;
import com.loopers.domain.stock.StockChangeInfo;
import com.loopers.domain.stock.StockInfo;
import com.loopers.domain.stock.StockService;
import com.loopers.interfaces.api.payment.PaymentDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentProcessor paymentProcessor;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final StockService stockService;
    private final PaymentGatewayPort paymentGatewayPort;
    private final RedisTemplate<String, String> redisTemplate;

    private final PaymentApplicationEventPublisher paymentEventPublisher;
    private final StockApplicationEventPublisher stockEventPublisher;

    private static final String RESULT_KEY_PREFIX = "payment:result:";
    private static final long RESULT_TTL_MINUTES = 10;

    @Transactional
    public PaymentResult initiatePayment(String userId, PaymentDto.V1.Initiate.Request request) {

        // 1. 주문 상태 검증
        Order order = orderService.findById(request.orderId());
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 가능한 상태가 아닙니다. 현재 상태: " + order.getStatus());
        }

        // 2. 결제 시점에 재고 처리
        processStockForPayment(order);

        // 3. 결제 방식에 따른 처리
        PaymentResult result;
        if (request.paymentMethod() == PaymentMethod.POINT) {
            result = processPointPayment(userId, request);
        } else {
            result = processPgPayment(userId, request);
        }

        log.info("결제 처리 시작 완료 - orderId: {}, method: {}, transactionKey: {}", request.orderId(), request.paymentMethod(),
            Objects.requireNonNull(result).transactionKey());

        return result;
    }

    /**
     * 재고 차감 및 예약 생성
     */
    private void processStockForPayment(Order order) {
        for (OrderLine line : order.getOrderLines()) {
            // 재고 확인
            StockInfo stockInfo = stockService.getStockInfo(line.getProductId());
            if (stockInfo.quantity() < line.getQuantity()) {
                throw new CoreException(ErrorType.CONFLICT,
                    String.format("재고 부족 - 상품ID: %d, 상품명: %s, 재고: %d, 요청: %d", line.getProductId(), line.getProductName(),
                        stockInfo.quantity(), line.getQuantity()));
            }

            // 재고 차감
            StockChangeInfo changeInfo = stockService.decreaseStock(line.getProductId(), line.getQuantity());

            // 재고 예약 생성 (나중에 확정/취소 처리용)
            stockService.createReservation(order.getId(), line.getProductId(), line.getQuantity());

            // 재고 이벤트 발행
            StockChanged event = StockChanged.from(
                changeInfo.productId(),
                changeInfo.previousQuantity(),
                changeInfo.currentQuantity(),
                "ORDER_PAYMENT"
            );
            stockEventPublisher.publish(event);
        }

        log.debug("재고 처리 완료 - orderId: {}", order.getId());
    }

    /**
     * 포인트 결제 처리
     */
    private PaymentResult processPointPayment(String userId, PaymentDto.V1.Initiate.Request request) {
        PaymentResult result = paymentProcessor.processPointPayment(
            new PaymentCommand.Point(request.orderId(), userId, request.toMoney()));

        if (result.isSuccess()) {
            paymentEventPublisher.publish(PaymentEvent.Completed.of(request.orderId()));
        } else {
            paymentEventPublisher.publish(PaymentEvent.Failed.of(request.orderId(), "POINT_PAYMENT_FAILED"));
        }

        return result;
    }

    /**
     * PG 결제 처리 (비동기이기 때문에 이벤트 발행 X, 콜백에서 처리)
     */
    private PaymentResult processPgPayment(String userId, PaymentDto.V1.Initiate.Request request) {
        return paymentProcessor.processPgPayment(new PaymentCommand.Pg(request.orderId(), userId, request.toMoney(),
            new PgPaymentInfo(request.cardType(), request.cardNo())));
    }

    /**
     * PG 결제 결과 처리 - 비동기 처리 Redis 멱등성 체크와 Resilience4j 재시도 로직 포함
     */
    @Async
    public void processPaymentResult(PaymentResultCommand command) {
        String resultKey = RESULT_KEY_PREFIX + command.transactionKey();

        // 1. 중복 결과 처리 체크
        Boolean isProcessed = redisTemplate.opsForValue()
            .setIfAbsent(resultKey, "processing", RESULT_TTL_MINUTES, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isProcessed)) {
            log.warn("중복 결제 결과 무시: transactionKey={}", command.transactionKey());
            return;
        }

        try {
            // 2. 결제 결과 처리 시도 (Resilience4j Retry 적용)
            processPaymentResultWithRetry(command);

            // 3. 처리 완료 표시
            redisTemplate.opsForValue().set(resultKey, "completed", RESULT_TTL_MINUTES, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.error("결제 결과 처리 최종 실패: transactionKey={}", command.transactionKey(), e);
            // 실패 시 키 삭제하여 재처리 가능하도록
            redisTemplate.delete(resultKey);
            throw e;
        }
    }

    /**
     * Resilience4j Retry를 사용한 결제 결과 처리 payment-result-process 설정 사용
     */
    @Retry(name = "payment-result-process", fallbackMethod = "processPaymentResultFallback")
    private void processPaymentResultWithRetry(PaymentResultCommand command) {
        paymentProcessor.processPaymentResult(command);
        log.info("결제 결과 처리 성공: transactionKey={}", command.transactionKey());
    }

    /**
     * 재시도 실패 시 Fallback 메서드
     */
    private void processPaymentResultFallback(PaymentResultCommand command, Exception ex) {
        log.error("결제 결과 처리 재시도 모두 실패: transactionKey={}, error={}", command.transactionKey(), ex.getMessage());
        throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 결과 처리 재시도 횟수 초과: " + command.transactionKey());
    }

    /**
     * PENDING 상태의 결제를 PG에서 확인하여 동기화
     */
    @Transactional
    public int synchronizePendingPayments(ZonedDateTime beforeTime) {
        List<PaymentInfo.Pending> pendingPayments = paymentService.findPendingPayments(beforeTime);
        int updatedCount = 0;

        for (PaymentInfo.Pending payment : pendingPayments) {
            try {
                if (synchronizePaymentStatus(payment)) {
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("결제 상태 동기화 실패: paymentId={}", payment.paymentId(), e);
            }
        }

        return updatedCount;
    }

    /**
     * 개별 결제 상태를 PG에서 확인하여 동기화
     */
    private boolean synchronizePaymentStatus(PaymentInfo.Pending payment) {
        String transactionKey = payment.transactionKey();
        if (transactionKey == null || transactionKey.isBlank()) {
            return false;
        }

        try {
            // PG에서 실제 상태 조회
            TransactionStatusResult result = paymentGatewayPort.getTransactionStatus(payment.userId(), transactionKey);

            if (result.isSuccess()) {
                paymentService.completePayment(transactionKey);
                orderService.updateOrderStatusToPaid(payment.orderId());
                return true;
            } else if (result.isFailed()) {
                paymentService.failPayment(transactionKey, result.reason());
                orderService.updateOrderStatusToPaymentFailed(payment.orderId());
                return true;
            }
        } catch (Exception e) {
            log.error("PG 상태 조회 실패: transactionKey={}", transactionKey, e);
        }

        return false;
    }

    /**
     * 타임아웃된 PENDING 결제를 실패 처리
     */
    @Transactional
    public int failTimeoutPayments(ZonedDateTime beforeTime) {
        List<PaymentInfo.Timeout> timeoutPayments = paymentService.findTimeoutPayments(beforeTime);

        for (PaymentInfo.Timeout payment : timeoutPayments) {
            try {
                paymentService.failPayment(payment.transactionKey(), "결제 처리 시간 초과");
                orderService.updateOrderStatusToPaymentFailed(payment.orderId());
            } catch (Exception e) {
                log.error("타임아웃 결제 처리 실패: paymentId={}", payment.paymentId(), e);
            }
        }

        return timeoutPayments.size();
    }

}
