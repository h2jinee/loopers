package com.loopers.domain.product;

public record ProductStockInfo(
    Long productId,
    Integer stock,
    boolean isAvailable
) {
    public ProductStockInfo(Long productId, Integer stock) {
        this(productId, stock, stock != null && stock > 0);
    }
}
