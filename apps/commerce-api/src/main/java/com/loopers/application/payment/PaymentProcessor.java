package com.loopers.application.payment;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.payment.strategy.PgPaymentStrategy;
import com.loopers.application.payment.strategy.PointPaymentStrategy;
import com.loopers.domain.common.Money;
import com.loopers.domain.payment.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessor {
    
    private final PaymentService paymentService;
    private final PointPaymentStrategy pointPaymentStrategy;
    private final PgPaymentStrategy pgPaymentStrategy;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 포인트 결제 처리
     * 독립 트랜잭션 - 포인트 차감과 결제 내역 저장이 원자적으로 처리
     */
    @Transactional
    public PaymentResult processPointPayment(PaymentCommand.Point command) {
        // 포인트 결제 실행
        PaymentResult result = pointPaymentStrategy.execute(
            PaymentCommand.Process.forPoint(command.orderId(), command.userId(), command.amount())
        );
        
        // 결제 내역 저장
        paymentService.savePaymentHistory(command.orderId(), result);
        
        log.info("포인트 결제 완료 - orderId: {}, amount: {}", 
            command.orderId(), result.amount());
        
        return result;
    }
    
    /**
     * PG 결제 처리
     * 독립 트랜잭션 - PG 결제와 결제 내역 저장이 원자적으로 처리
     */
    @Transactional
    public PaymentResult processPgPayment(PaymentCommand.Pg command) {
        // PG 결제 실행
        PaymentResult result = pgPaymentStrategy.execute(
            PaymentCommand.Process.forPg(command.orderId(), command.userId(), command.amount(), command.pgInfo())
        );
        
        // 결제 내역 저장
        paymentService.savePaymentHistory(command.orderId(), result);
        
        log.info("PG 결제 완료 - orderId: {}, amount: {}, txnId: {}", 
            command.orderId(), result.amount(), result.transactionId());
        
        return result;
    }
    
    /**
     * 포인트 결제 취소
     * 독립 트랜잭션 - 포인트 환불이 원자적으로 처리
     */
    @Transactional
    public void cancelPointPayment(Long orderId, String userId, Money amount) {
        // 포인트 환불
        pointPaymentStrategy.cancel(
            Payment.forPoint(orderId, userId, amount)
        );
        
        log.info("포인트 결제 취소 완료 - orderId: {}, amount: {}", orderId, amount);
    }
    
    /**
     * 결제 결과 처리
     * 독립 트랜잭션 - 결제 상태 업데이트만 처리, 주문 상태는 이벤트를 통해 분리
     */
    @Transactional
    public void processPaymentResult(PaymentResultCommand command) {
        try {
            if (command.success()) {
                handleSuccess(command);
            } else {
                handleFailure(command);
            }
        } catch (Exception e) {
            log.error("결제 결과 처리 중 오류: transactionKey={}, orderId={}", 
                command.transactionKey(), command.orderId(), e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 결과 처리 실패");
        }
    }

    private void handleSuccess(PaymentResultCommand command) {
        paymentService.completePayment(command.transactionKey());
        eventPublisher.publishEvent(PaymentEvent.Completed.fromCommand(command));
        log.info("결제 성공 처리 완료: orderId={}", command.orderId());
    }

    private void handleFailure(PaymentResultCommand command) {
        paymentService.failPayment(command.transactionKey(), command.failureReason());
        eventPublisher.publishEvent(PaymentEvent.Failed.fromCommand(command));
        log.warn("결제 실패 처리 완료: orderId={}, reason={}", 
            command.orderId(), command.failureReason());
    }
}
