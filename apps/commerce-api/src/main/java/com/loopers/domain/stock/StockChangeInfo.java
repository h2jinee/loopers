package com.loopers.domain.stock;

public record StockChangeInfo(
    Long productId,
    Integer previousQuantity,
    Integer currentQuantity
) {}
