package com.loopers.domain.order.vo;

public enum OrderStatus {
    PENDING,            // 주문 생성, 결제 대기
    PAID,               // 결제 완료
    PAYMENT_FAILED,     // 결제 실패
    COMPLETED,          // 주문 완료
    FAILED,             // 주문 실패
    PREPARING_SHIPMENT, // 배송 대기중
    SHIPPING,           // 배송 중
    DELIVERED,          // 배송 완료
    CANCELLED;          // 주문 취소
}
