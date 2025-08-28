package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.ProductStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

record ProductCacheDto(
    Long id,
    Long brandId,
    String nameKo,
    BigDecimal price,
    String description,
    ProductStatus status,
    Integer releaseYear,
    BigDecimal shippingFee,
    Long likeCount,
    ZonedDateTime createdAt
) {
    public static ProductCacheDto from(Product entity) {
        return new ProductCacheDto(
            entity.getId(),
            entity.getBrandId(),
            entity.getNameKo(),
            entity.getPrice().amount(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getReleaseYear(),
            entity.getShippingFee().amount(),
            entity.getLikeCount(),
            entity.getCreatedAt()
        );
    }
}
