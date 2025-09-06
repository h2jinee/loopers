package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import java.time.LocalDateTime;

public record PaymentResult(
    PaymentMethod method,
    Money amount,
    String transactionKey,
    PaymentResultStatus status,
    LocalDateTime processedAt,
    String message,
    String userId     // 결제한 사용자 ID
) {
    public static PaymentResult success(PaymentMethod method, Money amount, String transactionKey, String userId) {
        return new PaymentResult(
            method,
            amount,
            transactionKey,
            PaymentResultStatus.SUCCESS,
            LocalDateTime.now(),
            "결제 성공",
            userId
        );
    }

    public static PaymentResult pending(PaymentMethod method, Money amount, String transactionKey, String userId) {
        return new PaymentResult(
            method,
            amount,
            transactionKey,
            PaymentResultStatus.PENDING,
            LocalDateTime.now(),
            "결제 처리 중",
            userId
        );
    }

    public static PaymentResult failure(PaymentMethod method, String message, String userId) {
        return new PaymentResult(
            method,
            Money.ZERO,
            null,
            PaymentResultStatus.FAILED,
            LocalDateTime.now(),
            message,
            userId
        );
    }
    
    public boolean isSuccess() {
        return status == PaymentResultStatus.SUCCESS;
    }

    public boolean isPending() {
        return status == PaymentResultStatus.PENDING;
    }
}

enum PaymentResultStatus {
    SUCCESS,
    PENDING,
    FAILED,
    PARTIAL,
    CANCELLED
}
