package com.loopers.domain.stock;

public record StockInfo(
    Long productId,
    Integer quantity,
    boolean isAvailable
) {}