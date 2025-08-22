package com.loopers.domain.payment.command;

import com.loopers.domain.common.Money;

/**
 * PG 결제 취소 커맨드
 */
public record PgCancelCommand(
    String transactionId,
    String userId,
    Money amount,
    String reason
) {
    public PgCancelCommand {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null || amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("취소 금액은 0보다 커야 합니다.");
        }
    }
}
