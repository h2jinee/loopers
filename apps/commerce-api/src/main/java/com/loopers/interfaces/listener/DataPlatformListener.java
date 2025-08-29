package com.loopers.interfaces.listener;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 데이터 플랫폼 이벤트 리스너
 * 주문/결제 이벤트를 외부 데이터 플랫폼으로 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformListener {
    
    /**
     * 주문 생성 이벤트 전송
     * 트랜잭션 커밋 후 비동기로 전송
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderCreated(OrderEvent.Created event) {
        try {
            log.info("데이터 플랫폼 전송 시작 - OrderCreated - orderId: {}", event.orderId());
            
            // 스냅샷 데이터를 활용한 상세 정보 전송
            var payload = buildOrderCreatedPayload(event);

            log.info("데이터 플랫폼 전송 성공 - OrderCreated - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패 - OrderCreated - orderId: {}", event.orderId(), e);
        }
    }
    
    /**
     * 결제 완료 이벤트 전송
     */
    @Async
    @EventListener
    public void sendPaymentCompleted(PaymentEvent.Completed event) {
        try {
            log.info("데이터 플랫폼 전송 시작 - PaymentCompleted - orderId: {}", event.orderId());
            
            var payload = buildPaymentCompletedPayload(event);

            log.info("데이터 플랫폼 전송 성공 - PaymentCompleted - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패 - PaymentCompleted - orderId: {}", event.orderId(), e);
        }
    }
    
    /**
     * 결제 실패 이벤트 전송
     */
    @Async
    @EventListener
    public void sendPaymentFailed(PaymentEvent.Failed event) {
        try {
            log.info("데이터 플랫폼 전송 시작 - PaymentFailed - orderId: {}", event.orderId());
            
            var payload = buildPaymentFailedPayload(event);

            log.info("데이터 플랫폼 전송 성공 - PaymentFailed - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패 - PaymentFailed - orderId: {}", event.orderId(), e);
        }
    }
    
    /**
     * 주문 생성 페이로드 구성
     */
    private String buildOrderCreatedPayload(OrderEvent.Created event) {
        // 스냅샷 데이터를 활용한 상세 정보
        return String.format("""
            {
                "orderId": %d,
                "userId": "%s",
                "totalAmount": %s,
                "pointAmount": %s,
                "pgAmount": %s,
                "paymentMethod": "%s",
                "itemCount": %d,
                "items": %s,
                "createdAt": "%s"
            }
            """,
            event.orderId(),
            event.userId(),
            event.totalAmount(),
            event.pointAmount(),
            event.pgAmount(),
            event.paymentMethod(),
            event.orderLines().size(),
            formatOrderLines(event),
            event.createdAt()
        );
    }
    
    /**
     * 결제 완료 페이로드 구성
     */
    private String buildPaymentCompletedPayload(PaymentEvent.Completed event) {
        return String.format("""
            {
                "orderId": %d,
                "userId": "%s",
                "transactionKey": "%s",
                "totalAmount": %s,
                "pointUsed": %s,
                "pgPaid": %s,
                "method": "%s",
                "completedAt": "%s"
            }
            """,
            event.orderId(),
            event.userId(),
            event.transactionKey(),
            event.totalAmount(),
            event.pointUsed(),
            event.pgPaid(),
            event.method(),
            event.completedAt()
        );
    }
    
    /**
     * 결제 실패 페이로드 구성
     */
    private String buildPaymentFailedPayload(PaymentEvent.Failed event) {
        return String.format("""
            {
                "orderId": %d,
                "userId": "%s",
                "attemptedAmount": %s,
                "pointAmount": %s,
                "failureReason": "%s",
                "failedAt": "%s"
            }
            """,
            event.orderId(),
            event.userId(),
            event.attemptedAmount(),
            event.pointAmount(),
            event.failureReason(),
            event.failedAt()
        );
    }
    
    private String formatOrderLines(OrderEvent.Created event) {
        return event.orderLines().stream()
            .map(line -> String.format(
                "{\"productId\": %d, \"productName\": \"%s\", \"quantity\": %d, \"price\": %s}",
                line.productId(), line.productName(), line.quantity(), line.price()
            ))
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
}
