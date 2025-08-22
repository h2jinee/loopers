package com.loopers.domain.payment;

/**
 * PG 결제 요청 정보
 * Controller에서 받은 정보를 담는 DTO
 */
// TODO : 실제 API대로 수정
public record PgPaymentInfo(
    String cardNumber,
    String cardHolder,
    String expiryDate,
    String cvv
) {
    public PgPaymentInfo {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("카드 번호는 필수입니다.");
        }
        if (cardHolder == null || cardHolder.isBlank()) {
            throw new IllegalArgumentException("카드 소유자명은 필수입니다.");
        }
        if (expiryDate == null || expiryDate.isBlank()) {
            throw new IllegalArgumentException("유효기간은 필수입니다.");
        }
        if (cvv == null || cvv.isBlank()) {
            throw new IllegalArgumentException("CVV는 필수입니다.");
        }
    }
}
