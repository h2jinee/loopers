package com.loopers.application.service;

import com.loopers.application.service.dto.RankingBatchAggregation;
import com.loopers.infrastructure.redis.RankingRedisRepository;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import com.loopers.kafka.message.payload.OrderEventPayload;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRedisRepository rankingRedisRepository;
    private final RankingScoreCalculator scoreCalculator;

    /**
     * 배치 메시지 집계
     */
    public RankingBatchAggregation aggregateBatch(List<KafkaEventMessage<?>> messages) {
        RankingBatchAggregation aggregation = new RankingBatchAggregation();

        for (KafkaEventMessage<?> message : messages) {
            var payload = message.getPayload();

            switch (payload) {
                case CatalogEventPayload.ProductViewed viewed -> {
                    double score = scoreCalculator.calculateViewScore();
                    aggregation.accumulate(viewed.getProductId(), score);
                }

                case CatalogEventPayload.LikeChanged likeChanged -> {
                    double score = scoreCalculator.calculateLikeScore();
                    aggregation.accumulate(likeChanged.getProductId(), score);
                }

                case OrderEventPayload.OrderCreated orderCreated when
                    orderCreated.getOrderItems() != null -> {
                    orderCreated.getOrderItems().forEach(item -> {
                        double score = scoreCalculator.calculateOrderScore(
                            item.getPrice().longValue(),
                            item.getQuantity()
                        );
                        aggregation.accumulate(item.getProductId(), score);
                    });
                }

                default -> { /* 처리 안 함 */ }
            }
        }

        return aggregation;
    }

    /**
     * 집계된 점수 업데이트
     */
    public void updateScoresBatch(Map<Long, Double> scores) {
        if (scores.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        rankingRedisRepository.incrementScoresBatch(scores, today);

        log.info("배치 랭킹 업데이트 완료 - {} 개 상품", scores.size());
    }
}
