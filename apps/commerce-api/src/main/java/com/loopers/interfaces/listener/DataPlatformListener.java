package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 데이터 플랫폼 이벤트 리스너
 * 주문/결제 이벤트를 외부 데이터 플랫폼으로 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderCreated(OrderEvent.Created event) {
        try {
            // TODO : kafkaProducer.send("order.created", event);
            log.debug("OrderCreated sent - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("OrderCreated 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentCompleted(PaymentEvent.Completed event) {
        try {
            // kafkaProducer.send("payment.completed", event.orderId());
            log.debug("PaymentCompleted sent - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("PaymentCompleted 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentFailed(PaymentEvent.Failed event) {
        try {
            // kafkaProducer.send("payment.failed", Map.of(
            //     "orderId", event.orderId(),
            //     "reason", event.failureReason()
            // ));
            log.debug("PaymentFailed sent - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("PaymentFailed 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }
}
