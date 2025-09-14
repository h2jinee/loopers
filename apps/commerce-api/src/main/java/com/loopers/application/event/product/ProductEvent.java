package com.loopers.application.event.product;

import com.loopers.application.event.Event;
import java.time.LocalDateTime;

/**
 * 상품 도메인 이벤트
 */
public class ProductEvent {

    /**
     * 상품 조회 이벤트
     */
    public record Viewed(
        Long productId,
        LocalDateTime viewedAt
    ) implements Event {

        public static Viewed from(Long productId) {
            return new Viewed(productId, LocalDateTime.now());
        }

        @Override
        public LocalDateTime getOccurredAt() {
            return viewedAt;
        }

        @Override
        public String getAggregateId() {
            return String.valueOf(productId);
        }

        @Override
        public String getEventType() {
            return "ProductViewed";
        }
    }
}
