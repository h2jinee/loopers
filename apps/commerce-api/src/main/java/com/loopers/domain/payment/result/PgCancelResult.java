package com.loopers.domain.payment.result;

/**
 * PG 결제 취소 결과
 */
public record PgCancelResult(
    boolean isSuccess,
    String cancelId,
    String failureReason
) {
    public static PgCancelResult success(String cancelId) {
        return new PgCancelResult(true, cancelId, null);
    }
    
    public static PgCancelResult failure(String reason) {
        if (reason == null || reason.isBlank()) {
            reason = "취소 실패";
        }
        return new PgCancelResult(false, null, reason);
    }
}
