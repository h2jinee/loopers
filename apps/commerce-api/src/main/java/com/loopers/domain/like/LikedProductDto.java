package com.loopers.domain.like;

import com.loopers.domain.product.vo.ProductStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Getter;

@Getter
public class LikedProductDto {
    private final Long productId;
    private final Long brandId;
    private final String brandNameKo;
    private final String productNameKo;
    private final String description;
    private final BigDecimal price;
    private final Long likeCount;
    private final boolean isAvailable;
    private final ZonedDateTime likedAt;
    
    public LikedProductDto(
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
        this.productId = productId;
        this.brandId = brandId;
        this.brandNameKo = brandNameKo;
        this.productNameKo = productNameKo;
        this.description = description;
        this.price = price;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.isAvailable = status == ProductStatus.AVAILABLE;
        this.likedAt = likedAt;
    }

}
