package com.loopers.domain.like;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
        boolean isAvailable,
        ZonedDateTime likedAt
    ) {
        this.productId = productId;
        this.brandId = brandId;
        this.brandNameKo = brandNameKo;
        this.productNameKo = productNameKo;
        this.description = description;
        this.price = price;
        this.likeCount = likeCount;
        this.isAvailable = isAvailable;
        this.likedAt = likedAt;
    }
    
    public Long getProductId() { return productId; }
    public Long getBrandId() { return brandId; }
    public String getBrandNameKo() { return brandNameKo; }
    public String getProductNameKo() { return productNameKo; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Long getLikeCount() { return likeCount; }
    public boolean isAvailable() { return isAvailable; }
    public ZonedDateTime getLikedAt() { return likedAt; }
}
