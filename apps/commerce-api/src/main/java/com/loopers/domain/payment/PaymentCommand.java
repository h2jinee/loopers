package com.loopers.domain.payment;

import com.loopers.domain.order.OrderEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentCommand {
    
    public record ProcessPayment(
        OrderEntity order,
        String userId
    ) {
        public ProcessPayment {
            if (order == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 정보는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
}
