package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.vo.ProductStatus;
import java.math.BigDecimal;

public record ProductWithBrandDto(
    Long productId,
    Long brandId,
    String productNameKo,
    BigDecimal price,
    String description,
    ProductStatus status,
    Integer releaseYear,
    BigDecimal shippingFee,
    Long likeCount,
    String brandNameKo,
    String brandNameEn
) {
    public ProductWithBrandDto(ProductEntity product, BrandEntity brand, Long likeCount) {
        this(
            product.getId(),
            product.getBrandId(),
            product.getNameKo(),
            product.getPrice().amount(),
            product.getDescription(),
            product.getStatus(),
            product.getReleaseYear(),
            product.getShippingFee().amount(),
            likeCount != null ? likeCount : 0L,
            brand.getNameKo(),
            brand.getNameEn()
        );
    }
}
