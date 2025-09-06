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
    public static final String STOCK_CHANGED = "Changed";

    // 주문 이벤트
    public static final String ORDER_CREATED = "Created";
    public static final String ORDER_CONFIRMED = "Confirmed";
    public static final String ORDER_CANCELLED = "Cancelled";

    // 결제 이벤트
    public static final String PAYMENT_COMPLETED = "Completed";
    public static final String PAYMENT_FAILED = "Failed";

    private EventTypes() {
        throw new IllegalStateException("Constants class");
    }
}
