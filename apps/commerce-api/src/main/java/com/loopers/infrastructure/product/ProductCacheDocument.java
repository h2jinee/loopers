package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;

@Builder
public record ProductCacheDocument(
    Long id,
    String name,
    String description,
    Long price,
    Long brandId,
    String brandName,
    Long likeCount,
    Integer stockQuantity
) {
    public static ProductCacheDocument from(Product product) {
        return ProductCacheDocument.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .brandId(product.getBrand().getId())
            .brandName(product.getBrand().getName())
            .likeCount(product.getLikeCount())
            .stockQuantity(product.getStock().getQuantity())
            .build();
    }
}
