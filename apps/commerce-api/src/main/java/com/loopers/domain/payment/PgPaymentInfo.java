package com.loopers.domain.payment;

import java.util.List;

public record PgPaymentInfo(
    String cardType,
    String cardNo
) {
    private static final List<String> SUPPORTED_CARD_TYPES = List.of("SAMSUNG", "KB", "HYUNDAI");
    
    public PgPaymentInfo {
        if (cardType == null || cardType.isBlank()) {
            throw new IllegalArgumentException("카드 타입은 필수입니다.");
        }
        if (cardNo == null || cardNo.isBlank()) {
            throw new IllegalArgumentException("카드 번호는 필수입니다.");
        }
        if (!SUPPORTED_CARD_TYPES.contains(cardType)) {
            throw new IllegalArgumentException("지원하지 않는 카드 타입입니다: " + cardType);
        }
    }
}
