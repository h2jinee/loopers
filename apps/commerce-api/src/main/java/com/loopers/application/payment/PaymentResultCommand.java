package com.loopers.application.payment;

import com.loopers.application.event.order.OrderLineSnapshot;
import com.loopers.domain.payment.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public record PaymentResultCommand(
    String transactionKey,
    Long orderId,
    String userId,
    boolean success,
    String failureReason,
    BigDecimal totalAmount,
    BigDecimal pointUsed,
    BigDecimal pgPaid,
    PaymentMethod method,
    List<OrderLineSnapshot> orderLines
) {
    
    // 기본 결제 결과 처리를 위한 정적 팩토리 메서드
    public static PaymentResultCommand basicResult(String transactionKey, Long orderId, boolean success, String failureReason) {
        return new PaymentResultCommand(
            transactionKey, 
            orderId, 
            null, // userId
            success, 
            failureReason,
            BigDecimal.ZERO, // totalAmount
            BigDecimal.ZERO, // pointUsed
            BigDecimal.ZERO, // pgPaid
            PaymentMethod.PG, // method
            List.of() // orderLines
        );
    }
}
