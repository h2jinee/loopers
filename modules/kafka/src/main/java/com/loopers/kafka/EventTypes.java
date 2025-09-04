package com.loopers.kafka;

/**
 * 이벤트 타입 상수
 * eventType 필드에 사용되는 값들
 */
public class EventTypes {
    // Catalog Events (상품 도메인)
    public static final String LIKE_ADDED = "LikeAdded";
    public static final String LIKE_REMOVED = "LikeRemoved";
    public static final String STOCK_CHANGED = "StockChanged";

    // Order Events (주문 도메인)
    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_CANCELLED = "OrderCancelled";

    // private 생성자로 인스턴스화 방지
    private EventTypes() {
        throw new IllegalStateException("Constants class");
    }
}
