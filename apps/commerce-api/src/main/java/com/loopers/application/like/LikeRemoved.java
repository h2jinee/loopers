package com.loopers.application.like;

import java.time.LocalDateTime;

/**
 * 좋아요 삭제 이벤트
 */
public record LikeRemoved(
    String userId,
    Long productId,
    LocalDateTime removedAt
) {
    
    public static LikeRemoved from(String userId, Long productId) {
        return new LikeRemoved(userId, productId, LocalDateTime.now());
    }
}