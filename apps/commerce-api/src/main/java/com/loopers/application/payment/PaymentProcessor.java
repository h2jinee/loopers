package com.loopers.application.payment;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.payment.strategy.PgPaymentStrategy;
import com.loopers.application.payment.strategy.PointPaymentStrategy;
import com.loopers.domain.payment.*;
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
    private final PointPaymentStrategy pointStrategy;
    private final PgPaymentStrategy pgStrategy;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 포인트 결제 처리
     */
    @Transactional
    public PaymentResult processPointPayment(PaymentCommand.Point command) {
        return pointStrategy.execute(PaymentCommand.Process.forPoint(command.orderId(), command.userId(), command.amount()));
    }

    /**
     * PG 결제 처리
     */
    @Transactional
    public PaymentResult processPgPayment(PaymentCommand.Pg command) {
        return pgStrategy.execute(
            PaymentCommand.Process.forPg(command.orderId(), command.userId(), command.amount(), command.pgInfo()));
    }

    /**
     * PG 결제 결과 처리 (콜백)
     */
    @Transactional
    public void processPaymentResult(PaymentResultCommand command) {
        if (command.success()) {
            paymentService.completePayment(command.transactionKey());
            eventPublisher.publishEvent(PaymentEvent.Completed.of(command.orderId()));
        } else {
            paymentService.failPayment(command.transactionKey(), command.failureReason());
            eventPublisher.publishEvent(PaymentEvent.Failed.of(command.orderId(), command.failureReason()));
        }
    }
}
