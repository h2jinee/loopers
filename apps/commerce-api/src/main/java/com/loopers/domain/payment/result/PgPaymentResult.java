package com.loopers.domain.payment.result;

import com.loopers.domain.payment.PaymentStatus;

public record PgPaymentResult(
    PaymentStatus status,
    String transactionId,
    String failureReason
) {
    public static PgPaymentResult success(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다.");
        }
        return new PgPaymentResult(PaymentStatus.COMPLETED, transactionId, null);
    }
    
    public static PgPaymentResult failure(String reason) {
        if (reason == null || reason.isBlank()) {
            reason = "알 수 없는 오류";
        }
        return new PgPaymentResult(PaymentStatus.FAILED, null, reason);
    }
    
    public static PgPaymentResult pending(String transactionId, String reason) {
        return new PgPaymentResult(PaymentStatus.PENDING, transactionId, reason);
    }
    
    public boolean isSuccess() {
        return status == PaymentStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
}
