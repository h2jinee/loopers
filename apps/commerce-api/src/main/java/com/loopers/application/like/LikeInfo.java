package com.loopers.application.like;

public class LikeInfo {
    
    public record Result(
        boolean isLiked,
        Long likeCount
    ) {
        public static Result of(boolean isLiked, Long likeCount) {
            return new Result(isLiked, likeCount != null ? likeCount : 0L);
        }
    }
    
    public record LikedProduct(
        Long productId,
        Long brandId,
        String brandNameKo,
        String productNameKo,
        String description,
        java.math.BigDecimal price,
        Long likeCount,
        boolean isAvailable,
        java.time.ZonedDateTime likedAt
    ) {}
}
