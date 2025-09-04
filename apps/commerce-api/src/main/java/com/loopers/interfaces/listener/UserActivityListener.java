package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 유저 활동 추적 리스너
 * 모든 유저 행동을 로깅하고 메트릭 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityListener {

    private final MeterRegistry meterRegistry;  // Micrometer

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCreated(OrderEvent.Created event) {
        meterRegistry.counter("user.activity.payment.created", "userId", event.userId()).increment();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackPaymentCompleted(PaymentEvent.Completed event) {
        meterRegistry.counter("user.activity.payment.completed").increment();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackPaymentFailed(PaymentEvent.Failed event) {
        meterRegistry.counter("user.activity.payment.failed", "reason", event.failureReason()).increment();
    }
}
