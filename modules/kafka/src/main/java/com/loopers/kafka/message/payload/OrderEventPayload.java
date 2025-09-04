package com.loopers.kafka.message.payload;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 도메인 이벤트 페이로드
 */
public class OrderEventPayload {

    private OrderEventPayload() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 주문 생성 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderCreated {
        private Long orderId;
        private String userId;
        private BigDecimal totalAmount;
        private List<OrderItem> items;
        private LocalDateTime createdAt;
    }

    /**
     * 주문 확정 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderConfirmed {
        private Long orderId;
        private String userId;
        private LocalDateTime confirmedAt;
    }

    /**
     * 주문 취소 이벤트 페이로드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderCancelled {
        private Long orderId;
        private String userId;
        private String reason;  // PAYMENT_FAILED, CUSTOMER_REQUEST, STOCK_SHORTAGE
        private LocalDateTime cancelledAt;
    }

    /**
     * 주문 상품 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
