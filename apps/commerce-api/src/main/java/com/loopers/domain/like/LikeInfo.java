package com.loopers.domain.like;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class LikeInfo {
    
    public record LikeResult(
        boolean isLiked,
        Long likeCount
    ) {
        public static LikeResult of(boolean isLiked, Long likeCount) {
            return new LikeResult(isLiked, likeCount != null ? likeCount : 0L);
        }
    }
    
    public record LikedProduct(
        Long productId,
        Long brandId,
        String brandNameKo,
        String productNameKo,
        String description,
        BigDecimal price,
        Long likeCount,
        boolean isAvailable,
        ZonedDateTime likedAt
    ) {}
}