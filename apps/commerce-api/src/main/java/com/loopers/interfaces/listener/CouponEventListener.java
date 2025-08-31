package com.loopers.interfaces.listener;

import com.loopers.application.order.OrderCompleted;
import com.loopers.application.order.OrderCancelled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 이벤트 리스너 (부가 로직)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListener {
    
    /**
     * 주문 완료 이벤트 처리 - 쿠폰 사용 (부가 로직)
     */
    @Async
    @EventListener
    public void handleOrderCompleted(OrderCompleted event) {
        // 스냅샷으로 쿠폰 사용 필요성 판단
        if (!event.requiresCouponUsage()) {
            log.debug("쿠폰 사용 불필요 - orderId: {}", event.orderId());
            return;
        }
        
        log.info("주문 완료 이벤트 수신 - 쿠폰 사용 시작 - orderId: {}, discountAmount: {}", 
            event.orderId(), event.discountAmount());
        
        try {
            // TODO: CouponService 구현 후 실제 쿠폰 사용 로직
            // couponService.useCoupon(event.userId(), event.discountAmount(), event.orderId());
            
            log.info("쿠폰 사용 성공 - orderId: {}, discountAmount: {}", 
                event.orderId(), event.discountAmount());
            
        } catch (Exception e) {
            log.error("쿠폰 사용 실패 - orderId: {}, 주문은 유지됨 (부가 로직 실패)", event.orderId(), e);
        }
    }
    
    /**
     * 주문 취소 이벤트 처리 - 쿠폰 복원 (부가 로직)
     */
    @Async
    @EventListener
    public void handleOrderCancelled(OrderCancelled event) {
        log.info("주문 취소 이벤트 수신 - 쿠폰 복원 처리 - orderId: {}", event.orderId());
        
        try {
            // TODO: CouponService 구현 후 쿠폰 복원 로직
            log.info("쿠폰 복원 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("쿠폰 복원 실패 - orderId: {} (부가 로직 실패)", event.orderId(), e);
        }
    }
}
