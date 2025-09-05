package com.loopers.kafka;

/**
 * 이벤트 타입 상수
 * eventType 필드에 사용되는 값들
 */
public class EventTypes {
    // 좋아요 이벤트
    public static final String LIKE_ADDED = "Added";
    public static final String LIKE_REMOVED = "Removed";

    // 재고 이벤트
    public static final String STOCK_CHANGED = "StockChanged";

    // 주문 이벤트
    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_CANCELLED = "OrderCancelled";

    // 결제 이벤트
    public static final String PAYMENT_COMPLETED = "PaymentCompleted";
    public static final String PAYMENT_FAILED = "PaymentFailed";
}
