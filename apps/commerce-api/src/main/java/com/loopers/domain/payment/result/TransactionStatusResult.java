package com.loopers.domain.payment.result;

/**
 * PG 거래 상태 조회 결과
 */
public record TransactionStatusResult(
    TransactionStatus status,
    String reason
) {
    public enum TransactionStatus {
        SUCCESS,    // 결제 성공
        FAILED,     // 결제 실패
        PENDING,    // 처리 중
        UNKNOWN,    // 알 수 없음
        ERROR       // 조회 오류
    }
    
    public static TransactionStatusResult success() {
        return new TransactionStatusResult(TransactionStatus.SUCCESS, null);
    }
    
    public static TransactionStatusResult failed(String reason) {
        return new TransactionStatusResult(TransactionStatus.FAILED, reason);
    }
    
    public static TransactionStatusResult pending() {
        return new TransactionStatusResult(TransactionStatus.PENDING, null);
    }
    
    public static TransactionStatusResult unknown() {
        return new TransactionStatusResult(TransactionStatus.UNKNOWN, "상태를 확인할 수 없습니다");
    }
    
    public static TransactionStatusResult error(String message) {
        return new TransactionStatusResult(TransactionStatus.ERROR, message);
    }
    
    public boolean isSuccess() {
        return status == TransactionStatus.SUCCESS;
    }
    
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
    
    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }
}