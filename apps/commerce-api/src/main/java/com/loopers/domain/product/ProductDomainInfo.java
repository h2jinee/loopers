package com.loopers.domain.product;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.vo.ProductStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ProductDomainInfo(
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
    public static ProductDomainInfo from(ProductEntity entity) {
        return new ProductDomainInfo(
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
    
    public static ProductDomainInfo from(ProductCacheDto cacheDto) {
        return new ProductDomainInfo(
            cacheDto.id(),
            cacheDto.brandId(),
            cacheDto.nameKo(),
            cacheDto.price(),
            cacheDto.description(),
            cacheDto.status(),
            cacheDto.releaseYear(),
            cacheDto.shippingFee(),
            cacheDto.likeCount(),
            cacheDto.createdAt()
        );
    }
    
    public Money getPrice() {
        return Money.of(price);
    }

    public Money getShippingFee() {
        return Money.of(shippingFee);
    }
    
    public Money getTotalPrice() {
        return getPrice().add(getShippingFee());
    }
}
