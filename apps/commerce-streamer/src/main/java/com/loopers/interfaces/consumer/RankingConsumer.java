package com.loopers.interfaces.consumer;

import com.loopers.application.handler.RankingEventHandler;
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
 * 랭킹 집계 Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingConsumer {

    private static final String CONSUMER_NAME = "RANKING";

    private final RankingEventHandler rankingEventHandler;
    private final DlqPublisher dlqPublisher;

    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENTS, KafkaTopics.ORDER_EVENTS},
        groupId = "ranking-batch-group",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void consumeBatch(
        @Payload List<KafkaEventMessage<?>> messages,
        @Header(KafkaHeaders.RECEIVED_TOPIC) List<String> topics,
        Acknowledgment ack
    ) {
        log.info("랭킹 배치 수신 - {} 건", messages.size());

        try {
            // 배치 핸들러 위임
            rankingEventHandler.handleBatch(messages);

            // 성공 시 ACK
            ack.acknowledge();
            log.info("랭킹 배치 처리 완료 - {} 건", messages.size());

        } catch (Exception e) {
            log.error("랭킹 배치 처리 실패", e);

            // 실패한 배치 DLQ로 전달
            try {
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i) != null) {
                        dlqPublisher.sendToDlq(
                            topics.get(i),
                            messages.get(i),
                            CONSUMER_NAME,
                            "배치 프로세싱 실패 : " + e.getMessage()
                        );
                    }
                }
            } catch (Exception dlqError) {
                log.error("DLQ 전송 실패", dlqError);
            }

            // ACK
            ack.acknowledge();
        }
    }
}
