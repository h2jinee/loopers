package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 유저 활동 추적 리스너
 * 모든 유저 행동을 로깅하고 메트릭 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityListener {
    
    /**
     * 주문 생성 활동 추적
     */
    @Async
    @EventListener
    public void trackOrderCreated(OrderEvent.Created event) {
        try {
            log.info("[USER_ACTIVITY] ORDER_CREATED - userId: {}, orderId: {}, amount: {}, items: {}",
                event.userId(),
                event.orderId(),
                event.totalAmount(),
                event.orderLines().size()
            );
            
            // 상품별 주문 추적
            event.orderLines().forEach(line -> {
                log.debug("[USER_ACTIVITY] PRODUCT_ORDERED - userId: {}, productId: {}, quantity: {}",
                    event.userId(),
                    line.productId(),
                    line.quantity()
                );
            });
            
        } catch (Exception e) {
            log.error("유저 활동 추적 실패 - OrderCreated", e);
        }
    }
    
    /**
     * 주문 확정 활동 추적
     */
    @Async
    @EventListener
    public void trackOrderConfirmed(OrderEvent.Confirmed event) {
        log.info("[USER_ACTIVITY] ORDER_CONFIRMED - userId: {}, orderId: {}, confirmedAt: {}",
            event.userId(),
            event.orderId(),
            event.confirmedAt()
        );
    }
    
    /**
     * 주문 실패 활동 추적
     */
    @Async
    @EventListener
    public void trackOrderFailed(OrderEvent.Failed event) {
        log.info("[USER_ACTIVITY] ORDER_FAILED - userId: {}, orderId: {}, reason: {}",
            event.userId(),
            event.orderId(),
            event.reason()
        );
    }
    
    /**
     * 결제 성공 활동 추적
     */
    @Async
    @EventListener
    public void trackPaymentCompleted(PaymentEvent.Completed event) {
        log.info("[USER_ACTIVITY] PAYMENT_COMPLETED - userId: {}, orderId: {}, method: {}, amount: {}",
            event.userId(),
            event.orderId(),
            event.method(),
            event.totalAmount()
        );
        
        // 결제 수단별 메트릭
        if (event.pointUsed().compareTo(event.totalAmount()) == 0) {
            log.debug("[USER_ACTIVITY] POINT_PAYMENT - userId: {}, amount: {}",
                event.userId(),
                event.pointUsed()
            );
        } else if (event.pgPaid().compareTo(event.totalAmount()) == 0) {
            log.debug("[USER_ACTIVITY] PG_PAYMENT - userId: {}, amount: {}",
                event.userId(),
                event.pgPaid()
            );
        } else {
            log.debug("[USER_ACTIVITY] COMBINED_PAYMENT - userId: {}, point: {}, pg: {}",
                event.userId(),
                event.pointUsed(),
                event.pgPaid()
            );
        }
    }
    
    /**
     * 결제 실패 활동 추적
     */
    @Async
    @EventListener
    public void trackPaymentFailed(PaymentEvent.Failed event) {
        log.info("[USER_ACTIVITY] PAYMENT_FAILED - userId: {}, orderId: {}, reason: {}",
            event.userId(),
            event.orderId(),
            event.failureReason()
        );
        
        // 포인트 사용 여부에 따른 로깅
        if (event.pointAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.debug("[USER_ACTIVITY] PAYMENT_WITH_POINT_FAILED - userId: {}, pointAmount: {}",
                event.userId(), event.pointAmount());
        }
    }
}
