package com.loopers.application.payment;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.port.PaymentGatewayPort;
import com.loopers.domain.payment.result.TransactionStatusResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final PaymentGatewayPort paymentGatewayPort;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String RESULT_KEY_PREFIX = "payment:result:";
    private static final long RESULT_TTL_MINUTES = 10;
    
    /**
     * PG 결제 결과 처리 - 비동기 처리
     * Redis 멱등성 체크와 Resilience4j 재시도 로직 포함
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
     * Resilience4j Retry를 사용한 결제 결과 처리
     * payment-result-process 설정 사용
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
        log.error("결제 결과 처리 재시도 모두 실패: transactionKey={}, error={}", 
            command.transactionKey(), ex.getMessage());
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
        String transactionKey = payment.transactionId();
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
                paymentService.failPayment(payment.transactionId(), "결제 처리 시간 초과");
                orderService.updateOrderStatusToPaymentFailed(payment.orderId());
            } catch (Exception e) {
                log.error("타임아웃 결제 처리 실패: paymentId={}", payment.paymentId(), e);
            }
        }
        
        return timeoutPayments.size();
    }
    
}
