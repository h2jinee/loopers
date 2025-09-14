package com.loopers.interfaces.consumer.support;

import com.loopers.kafka.message.KafkaEventMessage;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqPublisher {

    private static final String DLQ_TOPIC_PREFIX = "dlq-";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendToDlq(
        String originalTopic,
        KafkaEventMessage<?> message,
        String consumerName,
        String errorMessage
    ) {
        String dlqTopic = DLQ_TOPIC_PREFIX + originalTopic;

        try {
            DlqMessage dlqMessage = DlqMessage.builder()
                .originalMessage(message)
                .originalTopic(originalTopic)
                .consumerName(consumerName)
                .errorMessage(errorMessage)
                .failedAt(Instant.now().toEpochMilli())
                .build();

            kafkaTemplate.send(dlqTopic, dlqMessage);
            log.info("DLQ 전송 성공 - topic: {}, eventId: {}", dlqTopic, message.getEventId());

        } catch (Exception e) {
            log.error("DLQ 전송 실패 - topic: {}, eventId: {}", dlqTopic, message.getEventId(), e);
        }
    }

    @Getter
    @Builder
    public static class DlqMessage {
        private final KafkaEventMessage<?> originalMessage;
        private final String originalTopic;
        private final String consumerName;
        private final String errorMessage;
        private final Long failedAt;
    }
}
