package com.loopers.domain.payment.vo;

import com.loopers.domain.payment.CardType;

/**
 * 카드 정보 Value Object
 * 비즈니스 규칙과 검증 로직 포함
 */
public record CardInfo(
    CardType cardType,
    String cardNumber,
    String cardHolder,
    String expiryDate,
    String cvv
) {
    private static final String CARD_NUMBER_PATTERN = "\\d{4}-\\d{4}-\\d{4}-\\d{4}";
    private static final String EXPIRY_DATE_PATTERN = "\\d{2}/\\d{2}";
    private static final String CVV_PATTERN = "\\d{3}";
    
    public CardInfo {
        validateCardType(cardType);
        validateCardNumber(cardNumber);
        validateCardHolder(cardHolder);
        validateExpiryDate(expiryDate);
        validateCvv(cvv);
    }
    
    private void validateCardType(CardType cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("카드 종류는 필수입니다.");
        }
    }
    
    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches(CARD_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("카드 번호 형식이 올바르지 않습니다. (xxxx-xxxx-xxxx-xxxx)");
        }
    }
    
    private void validateCardHolder(String cardHolder) {
        if (cardHolder == null || cardHolder.isBlank()) {
            throw new IllegalArgumentException("카드 소유자명은 필수입니다.");
        }
        if (cardHolder.length() > 50) {
            throw new IllegalArgumentException("카드 소유자명은 50자를 초과할 수 없습니다.");
        }
    }
    
    private void validateExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches(EXPIRY_DATE_PATTERN)) {
            throw new IllegalArgumentException("유효기간 형식이 올바르지 않습니다. (MM/YY)");
        }
    }
    
    private void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches(CVV_PATTERN)) {
            throw new IllegalArgumentException("CVV는 3자리 숫자여야 합니다.");
        }
    }
}
