package com.loopers.application.payment;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.application.point.PointFacade;
import com.loopers.domain.point.PointCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessor {
    
    private final PaymentService paymentService;
    private final PointFacade pointFacade;
    
    @Transactional
    public void processPayment(Order order, String userId) {
        // 1. 포인트 잔액 확인
        PointCommand.GetOne getPointCommand = new PointCommand.GetOne(userId);
        Money balance = pointFacade.getBalance(getPointCommand);
        
        if (balance.compareTo(order.getTotalAmount()) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                String.format("포인트가 부족합니다. 필요: %s, 현재: %s", 
                    order.getTotalAmount(), balance));
        }
        
        // 2. 결제 유효성 검증
        PaymentCommand.ProcessPayment paymentCommand = 
            new PaymentCommand.ProcessPayment(order, userId);
        paymentService.validatePayment(paymentCommand);
        
        // 3. 포인트 차감
        PointCommand.Use useCommand = new PointCommand.Use(
            userId, order.getTotalAmount(), order.getId()
        );
        pointFacade.use(useCommand);
        
        log.info("결제 처리 완료 - orderId: {}, userId: {}, amount: {}", 
            order.getId(), userId, order.getTotalAmount());
    }
    
    /**
     * 결제 취소 처리 (미구현)
     */
    @Transactional
    public void cancelPayment(Long orderId) {
        // TODO: 실제 결제 취소 로직 구현
        // 1. 결제 내역 조회
        // 2. 포인트 환불 처리
        
        log.info("결제 취소 처리 - orderId: {}", orderId);
    }
}
