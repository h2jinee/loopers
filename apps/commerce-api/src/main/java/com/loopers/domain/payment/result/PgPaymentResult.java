package com.loopers.domain.payment.result;

public record PgPaymentResult(
    boolean isSuccess,
    String transactionId,
    String failureReason
) {
    public static PgPaymentResult success(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다.");
        }
        return new PgPaymentResult(true, transactionId, null);
    }
    
    public static PgPaymentResult failure(String reason) {
        if (reason == null || reason.isBlank()) {
            reason = "알 수 없는 오류";
        }
        return new PgPaymentResult(false, null, reason);
    }
}
