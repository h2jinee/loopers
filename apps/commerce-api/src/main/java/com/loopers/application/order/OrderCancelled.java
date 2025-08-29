package com.loopers.application.order;

import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 */
public record OrderCancelled(
    Long orderId,
    String userId,
    String reason,
    LocalDateTime cancelledAt
) {
    
    public static OrderCancelled from(Long orderId, String userId, String reason) {
        return new OrderCancelled(orderId, userId, reason, LocalDateTime.now());
    }
}