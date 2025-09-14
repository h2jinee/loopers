package com.loopers.interfaces.consumer;

import com.loopers.application.support.dlq.DlqEventHandler;
import com.loopers.interfaces.consumer.support.DlqPublisher;
import com.loopers.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    private final DlqEventHandler dlqEventHandler;

    @KafkaListener(
        topics = KafkaTopics.DLQ_TOPIC,
        groupId = "dlq-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        @Payload DlqPublisher.DlqMessage dlqMessage,
        Acknowledgment ack
    ) {
        try {
            log.info("DLQ 메시지 수신: {}", dlqMessage);

            // Handler 위임
            dlqEventHandler.handleDlqMessage(dlqMessage);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("DLQ 메시지 처리 실패", e);
            // 실패하면 로그만 남기고 ACK
            ack.acknowledge();
        }
    }
}
