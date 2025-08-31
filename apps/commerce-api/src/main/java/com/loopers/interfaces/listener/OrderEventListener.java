package com.loopers.interfaces.listener;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.order.OrderFacade;
import com.loopers.domain.point.PointService;
import com.loopers.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 이벤트 리스너
 * 결제 완료/실패 이벤트를 받아 주문 상태를 변경하고 보상 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    
    private final OrderFacade orderFacade;
    private final StockService stockService;
    private final PointService pointService;
    
    /**
     * 결제 성공 이벤트 처리
     * 주문 상태를 확정으로 변경
     */
    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentEvent.Completed event) {
        log.info("결제 성공 이벤트 수신 - orderId: {}, transactionKey: {}", 
            event.orderId(), event.transactionKey());
        
        // 주문 확정 처리
        orderFacade.confirmOrder(event.orderId());
        
        log.info("주문 확정 완료 - orderId: {}, userId: {}, totalAmount: {}, items: {}", 
            event.orderId(), 
            event.userId(), 
            event.totalAmount(),
            event.orderLines().size()
        );
    }
    
    /**
     * 결제 실패 이벤트 처리
     * 주문 취소 및 보상 처리 (재고 복원, 포인트 복원)
     */
    @EventListener
    @Transactional
    public void handlePaymentFailed(PaymentEvent.Failed event) {
        log.info("결제 실패 이벤트 수신 - orderId: {}, reason: {}", 
            event.orderId(), event.failureReason());
        
        // 1. 주문 실패 처리
        orderFacade.failOrder(event.orderId(), event.failureReason());
        
        // 2. 재고 복원 (스냅샷)
        if (event.requiresStockRestore()) {
            restoreStock(event);
        }
        
        // 3. 포인트 복원 (포인트 사용된 경우)
        if (event.requiresPointRollback()) {
            restorePoint(event);
        }
        
        log.info("주문 실패 처리 완료 - orderId: {}", event.orderId());
    }
    
    /**
     * 재고 복원
     * 재고 예약 취소를 통해 자동으로 재고가 복원됨
     */
    private void restoreStock(PaymentEvent.Failed event) {
        try {
            // 재고 예약 취소 및 재고 자동 복원
            stockService.cancelReservationsAndRestoreStock(event.orderId());
            
            log.info("재고 복원 성공 - orderId: {}, items: {}", 
                event.orderId(), event.orderLines().size());
            
        } catch (Exception e) {
            log.error("재고 복원 실패 - orderId: {}, 수동 처리 필요", event.orderId(), e);
        }
    }
    
    /**
     * 포인트 복원
     * PG 결제 실패 시 차감된 포인트 복원
     */
    private void restorePoint(PaymentEvent.Failed event) {
        try {
            log.debug("포인트 복원 - userId: {}, amount: {}", 
                event.userId(), event.pointAmount());
            
            pointService.refundPoint(
                event.userId(), 
                com.loopers.domain.common.Money.of(event.pointAmount()),
                event.orderId()
            );
            
            log.info("포인트 복원 성공 - orderId: {}, userId: {}, amount: {}", 
                event.orderId(), event.userId(), event.pointAmount());
            
        } catch (Exception e) {
            log.error("포인트 복원 실패 - orderId: {}, userId: {}, 수동 처리 필요", 
                event.orderId(), event.userId(), e);
        }
    }
}
