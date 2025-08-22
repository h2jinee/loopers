package com.loopers.domain.payment.command;

import com.loopers.domain.common.Money;
import com.loopers.domain.payment.vo.CardInfo;

public record PgPaymentCommand(
    Long orderId,
    String userId,
    Money amount,
    CardInfo cardInfo
) {
    public PgPaymentCommand {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 주문 ID입니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null || amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        if (cardInfo == null) {
            throw new IllegalArgumentException("카드 정보는 필수입니다.");
        }
    }
}
