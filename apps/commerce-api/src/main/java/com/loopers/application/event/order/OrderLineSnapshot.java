package com.loopers.application.event.order;

import com.loopers.domain.order.OrderLine;
import java.math.BigDecimal;

/**
 * OrderLine 스냅샷
 */
public record OrderLineSnapshot(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal price,
    BigDecimal subtotal
) {
    public static OrderLineSnapshot from(OrderLine line) {
        return new OrderLineSnapshot(
            line.getProductId(),
            line.getProductName(),
            line.getQuantity(),
            line.getPrice().amount(),
            line.getPrice().amount().multiply(BigDecimal.valueOf(line.getQuantity()))
        );
    }
}
