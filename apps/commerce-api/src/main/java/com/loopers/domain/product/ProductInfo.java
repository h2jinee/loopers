package com.loopers.domain.product;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.vo.ProductStatus;
import java.time.ZonedDateTime;

public record ProductInfo(
    Long productId,
    Long brandId,
    String nameKo,
    String description,
    Money price,
    Money shippingFee,
    Money totalPrice,
    ProductStatus status,
    Integer releaseYear,
    Long likeCount,
    ZonedDateTime createdAt
) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(
            product.getId(),
            product.getBrandId(),
            product.getNameKo(),
            product.getDescription(),
            product.getPrice(),
            product.getShippingFee(),
            product.getTotalPrice(),
            product.getStatus(),
            product.getReleaseYear(),
            product.getLikeCount(),
            product.getCreatedAt()
        );
    }
    
    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }
}
