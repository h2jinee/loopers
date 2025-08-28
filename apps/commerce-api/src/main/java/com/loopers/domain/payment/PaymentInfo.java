package com.loopers.domain.payment;

public class PaymentInfo {
    
    /**
     * PENDING 상태의 결제 정보
     */
    public record Pending(
        Long paymentId,
        Long orderId,
        String userId,
        String transactionId
    ) {
        public static Pending from(Payment payment) {
            return new Pending(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getTransactionId()
            );
        }
    }
    
    /**
     * 타임아웃 대상 결제 정보
     */
    public record Timeout(
        Long paymentId,
        Long orderId,
        String transactionId
    ) {
        public static Timeout from(Payment payment) {
            return new Timeout(
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionId()
            );
        }
    }
    
    /**
     * 결제 요약 정보
     */
    public record Summary(
        Long paymentId,
        String userId,
        PaymentStatus status,
        String transactionId
    ) {
        public static Summary from(Payment payment) {
            return new Summary(
                payment.getId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getTransactionId()
            );
        }
    }
}
