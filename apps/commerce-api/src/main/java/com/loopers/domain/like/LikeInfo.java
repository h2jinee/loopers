package com.loopers.domain.like;

public class LikeInfo {
    
    public record LikeResult(
        boolean isLiked,
        Long likeCount
    ) {
        public static LikeResult of(boolean isLiked, Long likeCount) {
            return new LikeResult(isLiked, likeCount != null ? likeCount : 0L);
        }
    }
}
