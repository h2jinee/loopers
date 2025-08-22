package com.loopers.domain.order.vo;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    FAILED,
    PREPARING_SHIPMENT,
    SHIPPING,
    DELIVERED,
    CANCELLED;
}
