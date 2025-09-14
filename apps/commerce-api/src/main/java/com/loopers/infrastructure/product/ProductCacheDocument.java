package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import lombok.Builder;

@Builder
public record ProductCacheDocument(
    Long id,
    String nameKo,
    String description,
    Long price,
    Long brandId,
    Long likeCount
) {
    public static ProductCacheDocument from(Product product) {
        return ProductCacheDocument.builder()
            .id(product.getId())
            .nameKo(product.getNameKo())
            .description(product.getDescription())
            .price(product.getPrice().amount().longValue())
            .brandId(product.getBrandId())
            .likeCount(product.getLikeCount())
            .build();
    }
}
