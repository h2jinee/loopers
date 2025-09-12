package com.loopers.kafka.message.payload;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 카탈로그(상품) 도메인 이벤트 페이로드
 */
public class CatalogEventPayload {

    private CatalogEventPayload() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 좋아요 추가 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeAdded {
        private Long productId;
        private String userId;
        private LocalDateTime addedAt;
    }

    /**
     * 좋아요 제거 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeRemoved {
        private Long productId;
        private String userId;
        private LocalDateTime removedAt;
    }

    /**
     * 좋아요 변경 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeChanged {
        private Long productId;
        private Long totalLikeCount;  // 현재 총 좋아요 수 (절대값)
        private LocalDateTime changedAt;
    }

    /**
     * 재고 변경 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockChanged {
        private Long productId;
        private Integer previousQuantity;
        private Integer currentQuantity;
        private String changeReason;  // ORDERED, CANCELLED, ADJUSTED
        private LocalDateTime changedAt;
    }
}
