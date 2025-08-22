package com.loopers.infrastructure.payment.dto;

/**
 * PG Simulator의 결제 요청 응답 DTO
 * POST /api/v1/payments 응답
 */
public record PaymentResponse(
    String transactionKey,  // PG에서 생성한 거래 키
    String status,         // PENDING, SUCCESS, FAILED
    String reason          // 실패 사유 (실패 시에만)
) {
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}
