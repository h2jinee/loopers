package com.loopers.application.payment.strategy;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentStrategy;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentStrategy {
    
    private final PointService pointService;
    
    @Override
    public PaymentResult execute(PaymentCommand.Process command) {
        // 포인트 차감
        pointService.usePoint(
            command.userId(), 
            command.amount(), 
            command.orderId()
        );
        
        return PaymentResult.success(
            PaymentMethod.POINT,
            command.amount(),
            null,  // 포인트는 별도 거래 ID 없음
            command.userId()
        );
    }
    
    @Override
    public void cancel(Payment payment) {
        // 포인트 환불
        pointService.refundPoint(
            payment.getUserId(),
            payment.getAmount(),
            payment.getOrderId()
        );
    }
}