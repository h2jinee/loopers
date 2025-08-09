package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    public void processPayment(PaymentCommand.ProcessPaymentWithPoint command) {
        OrderEntity order = command.order();
        PointEntity point = command.point();
        
        if (!order.getStatus().isPaymentRequired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 상태가 아닙니다.");
        }
        
        if (order.isPaymentExpired()) {
            throw new CoreException(ErrorType.CONFLICT, "결제 시간이 만료되었습니다.");
        }
        
        Money orderAmount = order.getTotalAmount();
        
        if (!point.canPay(orderAmount)) {
            throw new CoreException(ErrorType.CONFLICT, 
                "포인트가 부족합니다. 필요 포인트: " + orderAmount.amount() + 
                ", 보유 포인트: " + point.getBalance().amount());
        }
    }
}
