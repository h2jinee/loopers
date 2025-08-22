package com.loopers.domain.payment.vo;

import com.loopers.domain.payment.CardType;

public record CardInfo(
    CardType cardType,
    String cardNo
) {
    private static final String CARD_NUMBER_PATTERN = "\\d{4}-\\d{4}-\\d{4}-\\d{4}";
    
    public CardInfo {
        validateCardType(cardType);
        validateCardNo(cardNo);
    }
    
    private void validateCardType(CardType cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("카드 종류는 필수입니다.");
        }
    }
    
    private void validateCardNo(String cardNo) {
        if (cardNo == null || !cardNo.matches(CARD_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("카드 번호 형식이 올바르지 않습니다. (xxxx-xxxx-xxxx-xxxx)");
        }
    }
}
