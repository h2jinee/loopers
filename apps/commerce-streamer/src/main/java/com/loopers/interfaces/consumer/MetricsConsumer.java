package com.loopers.interfaces.consumer;

import com.loopers.application.handler.MetricsEventHandler;
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

import java.util.List;

/**
 * 메트릭 집계 Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private static final String CONSUMER_NAME = "METRICS";

    private final MetricsEventHandler metricsEventHandler;
    private final DlqPublisher dlqPublisher;

    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENTS, KafkaTopics.ORDER_EVENTS},
        groupId = "metrics-batch-group",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void consumeBatch(
        @Payload List<KafkaEventMessage<?>> messages,
        @Header(KafkaHeaders.RECEIVED_TOPIC) List<String> topics,
        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
        @Header(KafkaHeaders.OFFSET) List<Long> offsets,
        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
        Acknowledgment ack
    ) {
        log.info("배치 처리 시작 - {} 건", messages.size());

        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (int i = 0; i < messages.size(); i++) {
            KafkaEventMessage<?> message = messages.get(i);
            String topic = topics.get(i);
            Integer partition = partitions.get(i);
            Long offset = offsets.get(i);

            if (message == null) {
                log.warn("null 메시지 스킵 - partition: {}, offset: {}", partition, offset);
                skippedCount++;
                continue;
            }

            try {
                log.debug("이벤트 처리 시작 - eventId: {}, key: {}, type: {}",
                    message.getEventId(), keys.get(i), message.getEventType());

                // Handler 위임
                boolean processed = metricsEventHandler.handleEvent(message);

                if (processed) {
                    processedCount++;
                    log.debug("이벤트 처리 완료 - eventId: {}", message.getEventId());
                } else {
                    skippedCount++;
                    log.debug("이벤트 스킵 (이미 처리됨 or 구버전) - eventId: {}", message.getEventId());
                }

            } catch (Exception e) {
                failedCount++;
                log.error("메시지 처리 실패 - eventId: {}, partition: {}, offset: {}",
                    message.getEventId(), partition, offset, e);

                // DLQ로 전송
                try {
                    dlqPublisher.sendToDlq(
                        topic,
                        message,
                        CONSUMER_NAME,
                        e.getClass().getSimpleName() + ": " + e.getMessage()
                    );
                    log.info("DLQ 전송 완료 - eventId: {}", message.getEventId());
                } catch (Exception dlqError) {
                    log.error("DLQ 전송 실패 - eventId: {}", message.getEventId(), dlqError);
                }
            }
        }

        // 배치 전체 ACK
        ack.acknowledge();
        log.info("배치 처리 완료 - 처리: {}, 스킵: {}, 실패: {} / 전체: {} 건",
            processedCount, skippedCount, failedCount, messages.size());
    }
}
