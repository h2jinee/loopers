package com.loopers.application.ranking;

import com.loopers.application.support.event.EventProcessingService;
import com.loopers.application.ranking.dto.RankingBatchAggregation;
import com.loopers.kafka.message.KafkaEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingEventHandler {

    private static final String CONSUMER_NAME = "RANKING";

    private final EventProcessingService eventProcessingService;
    private final RankingService rankingService;

    /**
     * 배치 메시지 처리
     */
    @Transactional
    public void handleBatch(List<KafkaEventMessage<?>> messages) {
        if (messages.isEmpty()) {
            return;
        }

        log.debug("랭킹 배치 처리 시작 - {} 건", messages.size());

        // 1. 중복 제거 후 처리 가능한 메시지 필터링
        List<KafkaEventMessage<?>> processableMessages = messages.stream()
            .filter(this::canProcess)
            .toList();

        if (processableMessages.isEmpty()) {
            log.debug("처리 가능한 메시지 없음");
            return;
        }

        // 2. 배치 집계
        RankingBatchAggregation aggregation = rankingService.aggregateBatch(processableMessages);

        // 3. Redis에 업데이트
        if (!aggregation.isEmpty()) {
            rankingService.updateScoresBatch(aggregation.getFinalScores());
            log.info("랭킹 배치 업데이트 완료 - {} 개 상품", aggregation.size());
        }

        // 4. 처리 완료 기록
        processableMessages.forEach(this::markAsProcessed);
    }

    private boolean canProcess(KafkaEventMessage<?> message) {
        if (message == null) {
            return false;
        }

        return eventProcessingService.canProcessEvent(
            message.getEventId(),
            message.getEventType(),
            message.getAggregateId(),
            CONSUMER_NAME,
            message.getVersion() != null ? message.getVersion() : 0L
        );
    }

    private void markAsProcessed(KafkaEventMessage<?> message) {
        eventProcessingService.markAsProcessed(
            message.getEventId(),
            CONSUMER_NAME,
            message.getEventType(),
            message.getAggregateId(),
            message.getVersion() != null ? message.getVersion() : 0L
        );
    }
}
