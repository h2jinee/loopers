package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.kafka.message.KafkaEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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

    private static final String DATA_PLATFORM_TOPIC = "data-platform-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderCreated(OrderEvent.Created event) {
        try {
            var message = KafkaEventMessage.of(
                "OrderCreated",
                event.orderId().toString(),
                event
            );

            kafkaTemplate.send(DATA_PLATFORM_TOPIC, event.orderId().toString(), message);
            log.info("OrderCreated 전송 완료 - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("OrderCreated 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentCompleted(PaymentEvent.Completed event) {
        try {
            var message = KafkaEventMessage.of(
                "PaymentCompleted",
                event.orderId().toString(),
                event
            );

            kafkaTemplate.send(DATA_PLATFORM_TOPIC, event.orderId().toString(), message);
            log.info("PaymentCompleted 전송 완료 - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("PaymentCompleted 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaymentFailed(PaymentEvent.Failed event) {
        try {
            var message = KafkaEventMessage.of(
                "PaymentFailed",
                event.orderId().toString(),
                event
            );

            kafkaTemplate.send(DATA_PLATFORM_TOPIC, event.orderId().toString(), message);
            log.info("PaymentFailed 전송 완료 - orderId: {}, reason: {}",
                event.orderId(), event.failureReason());
        } catch (Exception e) {
            log.error("PaymentFailed 전송 실패 - orderId: {}", event.orderId(), e);
        }
    }
}
