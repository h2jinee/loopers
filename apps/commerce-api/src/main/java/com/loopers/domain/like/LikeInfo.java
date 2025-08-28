package com.loopers.domain.like;

import com.loopers.domain.product.vo.ProductStatus;
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
    
    /**
     * 좋아요한 상품 정보 (Domain Info)
     */
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
    ) {
        public static LikedProduct of(
            Long productId,
            Long brandId,
            String brandNameKo,
            String productNameKo,
            String description,
            BigDecimal price,
            Long likeCount,
            ProductStatus status,
            ZonedDateTime likedAt
        ) {
            return new LikedProduct(
                productId,
                brandId,
                brandNameKo,
                productNameKo,
                description,
                price,
                likeCount != null ? likeCount : 0L,
                status == ProductStatus.AVAILABLE,
                likedAt
            );
        }
    }
}
