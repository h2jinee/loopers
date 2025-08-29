package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.payment.PaymentProcessor;
import com.loopers.domain.common.Money;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 이벤트 리스너
 * 주문 생성 이벤트를 받아 포인트 결제만 자동 처리
 * PG 결제는 별도 이벤트나 API를 통해 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    
    private final PaymentProcessor paymentProcessor;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 주문 생성 이벤트 수신 후 결제 처리
     * 포인트 결제만 자동으로 처리하고, PG 결제는 대기
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderEvent.Created event) {
        log.info("주문 이벤트 수신 - orderId: {}, paymentMethod: {}, totalAmount: {}",
            event.orderId(), event.paymentMethod(), event.totalAmount());
        
        // 포인트 결제만 자동 처리
        if (event.paymentMethod() == PaymentMethod.POINT) {
            processPointPayment(event);
        } else if (event.paymentMethod() == PaymentMethod.PG) {
            // PG 결제는 별도 처리 대기
            log.info("PG 결제 대기 중 - orderId: {}, amount: {}", 
                event.orderId(), event.pgAmount());
            // TODO: PG 결제 요청 이벤트 발행 또는 상태 저장
        } else if (event.paymentMethod() == PaymentMethod.COMBINED) {
            // 복합 결제도 별도 처리 대기 (PG 정보 필요)
            log.info("복합 결제 대기 중 - orderId: {}, pointAmount: {}, pgAmount: {}", 
                event.orderId(), event.pointAmount(), event.pgAmount());
            // TODO: 복합 결제 처리 로직
        }
    }
    
    /**
     * 포인트 결제 처리
     */
    private void processPointPayment(OrderEvent.Created event) {
        log.info("포인트 결제 처리 시작 - orderId: {}, amount: {}",
            event.orderId(), event.pointAmount());
        
        try {
            PaymentResult result = paymentProcessor.processPointPayment(
                new PaymentCommand.Point(
                    event.orderId(),
                    event.userId(),
                    Money.of(event.pointAmount())
                )
            );
            
            // 결제 성공 이벤트 발행
            eventPublisher.publishEvent(
                PaymentEvent.Completed.from(event, result)
            );
            
            log.info("포인트 결제 성공 - orderId: {}, transactionKey: {}", 
                event.orderId(), result.transactionId());
            
        } catch (Exception e) {
            log.error("포인트 결제 실패 - orderId: {}", event.orderId(), e);
            
            // 실패 이벤트 발행
            eventPublisher.publishEvent(
                PaymentEvent.Failed.from(event, e)
            );
        }
    }
    
    /**
     * PG 결제 시작 이벤트 처리 (별도 API나 이벤트로 트리거)
     * 실제 PG 정보를 받아서 처리
     */
    public void processPgPayment(Long orderId, String userId, Money amount, 
                                  com.loopers.domain.payment.PgPaymentInfo pgInfo) {
        log.info("PG 결제 처리 시작 - orderId: {}, amount: {}", orderId, amount);
        
        try {
            PaymentResult result = paymentProcessor.processPgPayment(
                new PaymentCommand.Pg(orderId, userId, amount, pgInfo)
            );
            
            // 결제 성공 이벤트 발행
            eventPublisher.publishEvent(
                new PaymentEvent.Completed(
                    orderId,
                    userId,
                    result.transactionId(),
                    amount.amount(),
                    BigDecimal.ZERO,
                    amount.amount(),
                    PaymentMethod.PG,
                    LocalDateTime.now(),
                    List.of()  // OrderLine 정보는 별도로 조회 필요
                )
            );
            
        } catch (Exception e) {
            log.error("PG 결제 실패 - orderId: {}", orderId, e);
            
            eventPublisher.publishEvent(
                new PaymentEvent.Failed(
                    orderId,
                    userId,
                    amount.amount(),
                    BigDecimal.ZERO,
                    e.getMessage(),
                    LocalDateTime.now(),
                    List.of()
                )
            );
        }
    }
    
    /**
     * 복합 결제 처리 (포인트 + PG)
     * 별도 이벤트나 API로 PG 정보를 받은 후 처리
     */
    public void processCombinedPayment(Long orderId, String userId, 
                                       Money pointAmount, Money pgAmount,
                                       com.loopers.domain.payment.PgPaymentInfo pgInfo) {
        log.info("복합 결제 처리 시작 - orderId: {}, pointAmount: {}, pgAmount: {}",
            orderId, pointAmount, pgAmount);
        
        // 1. 포인트 결제
        try {
            PaymentResult pointResult = paymentProcessor.processPointPayment(
                new PaymentCommand.Point(orderId, userId, pointAmount)
            );
            
            // 2. PG 결제
            try {
                PaymentResult pgResult = paymentProcessor.processPgPayment(
                    new PaymentCommand.Pg(orderId, userId, pgAmount, pgInfo)
                );
                
                // 복합 결제 성공
                PaymentResult combined = PaymentResult.combined(pointResult, pgResult);
                
                eventPublisher.publishEvent(
                    new PaymentEvent.Completed(
                        orderId,
                        userId,
                        combined.transactionId(),
                        pointAmount.amount().add(pgAmount.amount()),
                        pointAmount.amount(),
                        pgAmount.amount(),
                        PaymentMethod.COMBINED,
                        LocalDateTime.now(),
                        List.of()
                    )
                );
                
            } catch (Exception pgException) {
                // PG 실패 시 포인트 롤백
                log.error("PG 결제 실패, 포인트 롤백 - orderId: {}", orderId, pgException);
                paymentProcessor.cancelPointPayment(orderId, userId, pointAmount);
                throw pgException;
            }
            
        } catch (Exception e) {
            log.error("복합 결제 실패 - orderId: {}", orderId, e);
            
            eventPublisher.publishEvent(
                new PaymentEvent.Failed(
                    orderId,
                    userId,
                    pointAmount.amount().add(pgAmount.amount()),
                    pointAmount.amount(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    List.of()
                )
            );
        }
    }
}
