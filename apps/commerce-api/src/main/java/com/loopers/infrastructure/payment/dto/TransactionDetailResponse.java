package com.loopers.infrastructure.payment.dto;

/**
 * PG Simulator의 거래 상세 조회 응답 DTO
 * GET /api/v1/payments/{transactionKey} 응답
 */
public record TransactionDetailResponse(
    String transactionKey,
    String orderId,
    String cardType,  // PG는 String으로 반환 (SAMSUNG, KB, HYUNDAI)
    String cardNo,
    Long amount,
    String status,    // PENDING, SUCCESS, FAILED
    String reason
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
