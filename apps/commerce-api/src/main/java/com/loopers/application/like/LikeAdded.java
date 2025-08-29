package com.loopers.application.like;

import java.time.LocalDateTime;

/**
 * 좋아요 추가 이벤트
 * 좋아요는 동기(메인 로직), 집계는 비동기(부가 로직)
 */
public record LikeAdded(
    String userId,
    Long productId,
    LocalDateTime addedAt
) {
    
    public static LikeAdded from(String userId, Long productId) {
        return new LikeAdded(userId, productId, LocalDateTime.now());
    }
}