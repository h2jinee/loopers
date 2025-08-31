package com.loopers.application.order;

import java.time.LocalDateTime;

/**
 * 주문 확정 이벤트
 */
public record OrderConfirmed(
    Long orderId,
    String userId,
    LocalDateTime confirmedAt
) {
    
    public static OrderConfirmed from(Long orderId, String userId) {
        return new OrderConfirmed(orderId, userId, LocalDateTime.now());
    }
}