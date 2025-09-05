package com.loopers.application.stock;

import java.time.LocalDateTime;

/**
 * 재고 변경 이벤트
 */
public record StockChanged(
    Long productId,
    Integer previousQuantity,
    Integer currentQuantity,
    String changeReason,  // "ORDER_PAYMENT", "ORDER_CANCELLED"
    LocalDateTime changedAt
) {
    public static StockChanged from(Long productId, Integer previousQuantity, Integer currentQuantity, String reason) {
        return new StockChanged(productId, previousQuantity, currentQuantity, reason, LocalDateTime.now());
    }
}
