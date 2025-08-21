package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    /**
     * 결제 유효성 검증
     */
    public void validatePayment(PaymentCommand.ProcessPayment command) {
        Order order = command.order();
        
        if (!order.getStatus().isPaymentRequired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 상태가 아닙니다.");
        }
        
        if (order.isPaymentExpired()) {
            throw new CoreException(ErrorType.CONFLICT, "결제 시간이 만료되었습니다.");
        }
    }
}
