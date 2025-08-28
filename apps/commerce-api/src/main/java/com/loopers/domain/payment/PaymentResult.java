package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import java.time.LocalDateTime;

public record PaymentResult(
    PaymentMethod method,
    Money amount,
    String transactionId,
    PaymentResultStatus status,
    LocalDateTime processedAt,
    String message,
    Money pointUsed,  // 사용된 포인트 (복합 결제용)
    Money pgPaid,     // PG로 결제된 금액 (복합 결제용)
    String userId     // 결제한 사용자 ID
) {
    // 단일 결제 성공 (포인트 또는 PG)
    public static PaymentResult success(PaymentMethod method, Money amount, String transactionId, String userId) {
        Money pointUsed = method == PaymentMethod.POINT ? amount : Money.ZERO;
        Money pgPaid = method == PaymentMethod.PG ? amount : Money.ZERO;
        
        return new PaymentResult(
            method,
            amount,
            transactionId,
            PaymentResultStatus.SUCCESS,
            LocalDateTime.now(),
            "결제 성공",
            pointUsed,
            pgPaid,
            userId
        );
    }
    
    // 복합 결제 성공 (포인트 + PG)
    public static PaymentResult combined(PaymentResult pointResult, PaymentResult pgResult) {
        Money totalAmount = pointResult.amount().add(pgResult.amount());
        
        return new PaymentResult(
            PaymentMethod.COMBINED,
            totalAmount,
            pgResult.transactionId(),  // PG 거래 ID 사용
            PaymentResultStatus.SUCCESS,
            LocalDateTime.now(),
            "복합 결제 성공",
            pointResult.amount(),  // 포인트 사용액
            pgResult.amount(),      // PG 결제액
            pointResult.userId()    // 사용자 ID (두 결과 모두 같은 사용자)
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
            Money.ZERO,
            Money.ZERO,
            userId
        );
    }
    
    public boolean isSuccess() {
        return status == PaymentResultStatus.SUCCESS;
    }
    
    public boolean isPartialSuccess() {
        return status == PaymentResultStatus.PARTIAL;
    }
    
    public boolean isCombinedPayment() {
        return method == PaymentMethod.COMBINED;
    }
}

enum PaymentResultStatus {
    SUCCESS,
    FAILED,
    PARTIAL,
    CANCELLED
}
