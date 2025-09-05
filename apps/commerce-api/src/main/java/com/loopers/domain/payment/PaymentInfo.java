package com.loopers.domain.payment;

public class PaymentInfo {
    
    /**
     * PENDING 상태의 결제 정보
     */
    public record Pending(
        Long paymentId,
        Long orderId,
        String userId,
        String transactionKey
    ) {
        public static Pending from(Payment payment) {
            return new Pending(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getTransactionKey()
            );
        }
    }
    
    /**
     * 타임아웃 대상 결제 정보
     */
    public record Timeout(
        Long paymentId,
        Long orderId,
        String transactionKey
    ) {
        public static Timeout from(Payment payment) {
            return new Timeout(
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionKey()
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
        String transactionKey
    ) {
        public static Summary from(Payment payment) {
            return new Summary(
                payment.getId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getTransactionKey()
            );
        }
    }
}
