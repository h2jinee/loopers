package com.loopers.interfaces.consumer;

import com.loopers.application.handler.CacheEvictionHandler;
import com.loopers.interfaces.consumer.support.DlqPublisher;
import com.loopers.kafka.KafkaTopics;
import com.loopers.kafka.message.KafkaEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionConsumer {

    private static final String CONSUMER_NAME = "CACHE_EVICT";

    private final CacheEvictionHandler cacheEvictionHandler;
    private final DlqPublisher dlqPublisher;

    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENTS},
        groupId = "cache-eviction-group",
        containerFactory = "kafkaListenerContainerFactory"  // 단일 메시지용
    )
    public void consume(
        @Payload KafkaEventMessage<?> message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment ack
    ) {
        String eventId = message != null ? message.getEventId() : "unknown";

        try {
            if (message == null) {
                log.warn("null 메시지 수신 - partition: {}, offset: {}", partition, offset);
                ack.acknowledge();
                return;
            }

            log.debug("캐시 무효화 처리 시작 - eventId: {}, type: {}, topic: {}",
                eventId, message.getEventType(), topic);

            // Handler 위임
            boolean processed = cacheEvictionHandler.handleEvent(message);

            if (processed) {
                log.debug("캐시 무효화 완료 - eventId: {}", eventId);
            } else {
                log.debug("캐시 무효화 스킵 (불필요 or 이미 처리됨) - eventId: {}", eventId);
            }

            // ACK
            ack.acknowledge();

        } catch (Exception e) {
            log.error("캐시 무효화 실패 - eventId: {}, error: {}",
                eventId, e.getMessage(), e);

            // DLQ로 전송
            try {
                dlqPublisher.sendToDlq(
                    topic,
                    message,
                    CONSUMER_NAME,
                    e.getClass().getSimpleName() + ": " + e.getMessage()
                );
                log.info("DLQ 전송 완료 - eventId: {}", eventId);
            } catch (Exception dlqError) {
                log.error("DLQ 전송 실패 - eventId: {}", eventId, dlqError);
            }

            // ACK는 하되, DLQ로 보냈음을 표시
            ack.acknowledge();
        }
    }
}
