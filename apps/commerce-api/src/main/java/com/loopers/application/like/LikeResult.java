package com.loopers.application.like;

import com.loopers.domain.like.LikeInfo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class LikeResult {
    
    public record LikeToggleResult(
        boolean isLiked,
        Long likeCount
    ) {
        public static LikeToggleResult from(LikeInfo.LikeResult domainInfo) {
            return new LikeToggleResult(
                domainInfo.isLiked(),
                domainInfo.likeCount()
            );
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
    ) {
        public static LikedProduct from(LikeInfo.LikedProduct domainInfo) {
            return new LikedProduct(
                domainInfo.productId(),
                domainInfo.brandId(),
                domainInfo.brandNameKo(),
                domainInfo.productNameKo(),
                domainInfo.description(),
                domainInfo.price(),
                domainInfo.likeCount(),
                domainInfo.isAvailable(),
                domainInfo.likedAt()
            );
        }
    }
}