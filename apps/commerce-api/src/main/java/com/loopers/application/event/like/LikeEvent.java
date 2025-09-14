package com.loopers.application.event.like;

import com.loopers.application.event.Event;
import java.time.LocalDateTime;

/**
 * 좋아요 도메인 이벤트
 */
public class LikeEvent {

    /**
     * 좋아요 카운트 변경 이벤트
     */
    public record CountChanged(
        Long productId,
        Long totalLikeCount,  // 현재 총 좋아요 수
        LocalDateTime changedAt
    ) implements Event {

        public static CountChanged from(Long productId, Long totalLikeCount) {
            return new CountChanged(productId, totalLikeCount, LocalDateTime.now());
        }

        @Override
        public LocalDateTime getOccurredAt() {
            return changedAt;
        }

        @Override
        public String getAggregateId() {
            return String.valueOf(productId);
        }
    }
}
