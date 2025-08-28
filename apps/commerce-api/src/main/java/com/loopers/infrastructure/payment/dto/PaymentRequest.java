package com.loopers.infrastructure.payment.dto;

/**
 * PG Simulator 결제 요청 DTO
 * POST /api/v1/payments 요청
 */
public record PaymentRequest(
    String orderId,
    String cardType,
    String cardNo,
    Long amount,
    String callbackUrl
) {
    public PaymentRequest {
        if (orderId == null || orderId.isBlank() || orderId.length() < 6) {
            throw new IllegalArgumentException("orderId는 6자리 이상이어야 합니다.");
        }
        if (cardType == null || cardType.isBlank()) {
            throw new IllegalArgumentException("cardType은 필수입니다.");
        }
        if (cardNo == null || !cardNo.matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$")) {
            throw new IllegalArgumentException("cardNo는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }
        if (callbackUrl == null || !callbackUrl.startsWith("http://localhost:8080")) {
            throw new IllegalArgumentException("callbackUrl은 http://localhost:8080으로 시작해야 합니다.");
        }
    }
}
