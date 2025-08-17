package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ProductCacheDto(
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
    public static ProductCacheDto from(ProductEntity entity) {
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
