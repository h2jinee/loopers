package com.loopers.interfaces.listener;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.order.OrderFacade;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.PointService;
import com.loopers.domain.stock.StockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 이벤트 처리 리스너
 * 결제 도메인 이벤트를 받아 필요한 후속 처리 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderFacade orderFacade;
    private final StockService stockService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handlePaymentCompleted(PaymentEvent.Completed event) {
        log.info("결제 성공 처리 - orderId: {}", event.orderId());

        // 1. 주문 확정
        orderFacade.confirmOrder(event.orderId());

        // 2. 재고 예약 확정
        stockService.confirmReservations(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handlePaymentFailed(PaymentEvent.Failed event) {
        log.info("결제 실패 처리 - orderId: {}", event.orderId());

        // 1. 주문 취소
        orderFacade.cancelOrder(event.orderId(), "PAYMENT_FAILED");

        // 2. 재고 롤백
        stockService.rollbackStock(event.orderId());
    }
}
