package com.loopers.application.event.payment;

/**
 * 결제 도메인 이벤트
 */
public class PaymentEvent {

    public record Completed(Long orderId) {
        public static Completed of(Long orderId) {
            return new Completed(orderId);
        }
    }

    public record Failed(
        Long orderId,
        String failureReason
    ) {
        public static Failed of(Long orderId, String reason) {
            return new Failed(orderId, reason);
        }
    }
}
