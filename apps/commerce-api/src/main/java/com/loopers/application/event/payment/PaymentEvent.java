package com.loopers.application.event.payment;

import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.event.order.OrderLineSnapshot;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 도메인 이벤트
 */
public class PaymentEvent {
    
    /**
     * PG 결제 요청 이벤트
     * 별도 API나 외부 트리거로 발생
     */
    public record PgPaymentRequested(
        Long orderId,
        String userId,
        BigDecimal amount,
        com.loopers.domain.payment.PgPaymentInfo pgInfo,
        LocalDateTime requestedAt
    ) {
        public static PgPaymentRequested create(Long orderId, String userId, 
                                               BigDecimal amount, 
                                               com.loopers.domain.payment.PgPaymentInfo pgInfo) {
            return new PgPaymentRequested(orderId, userId, amount, pgInfo, LocalDateTime.now());
        }
    }
    
    /**
     * 결제 완료 이벤트
     */
    public record Completed(
        Long orderId,
        String userId,
        String transactionKey,
        BigDecimal totalAmount,
        BigDecimal pointUsed,
        BigDecimal pgPaid,
        PaymentMethod method,
        LocalDateTime completedAt,
        List<OrderLineSnapshot> orderLines  // 데이터 플랫폼 전송용
    ) {
        
        public static Completed from(OrderEvent.Created orderEvent, PaymentResult result) {
            return new Completed(
                orderEvent.orderId(),
                orderEvent.userId(),
                result.transactionId(),
                orderEvent.totalAmount(),
                orderEvent.pointAmount(),
                orderEvent.pgAmount(),
                orderEvent.paymentMethod(),
                LocalDateTime.now(),
                orderEvent.orderLines()
            );
        }
        
        public static Completed fromCommand(com.loopers.application.payment.PaymentResultCommand command) {
            return new Completed(
                command.orderId(),
                command.userId(),
                command.transactionKey(),
                command.totalAmount(),
                command.pointUsed(),
                command.pgPaid(),
                command.method(),
                LocalDateTime.now(),
                command.orderLines()
            );
        }
    }
    
    /**
     * 결제 실패 이벤트
     */
    public record Failed(
        Long orderId,
        String userId,
        BigDecimal attemptedAmount,
        BigDecimal pointAmount,  // 롤백 필요한 포인트
        String failureReason,
        LocalDateTime failedAt,
        List<OrderLineSnapshot> orderLines  // 재고 복원용
    ) {
        
        public static Failed from(OrderEvent.Created orderEvent, Exception e) {
            return new Failed(
                orderEvent.orderId(),
                orderEvent.userId(),
                orderEvent.totalAmount(),
                orderEvent.pointAmount(),
                e.getMessage(),
                LocalDateTime.now(),
                orderEvent.orderLines()
            );
        }
        
        public static Failed fromCommand(com.loopers.application.payment.PaymentResultCommand command) {
            return new Failed(
                command.orderId(),
                command.userId(),
                command.totalAmount(),
                command.pointUsed(),
                command.failureReason(),
                LocalDateTime.now(),
                command.orderLines()
            );
        }
        
        // 포인트가 사용되었으면 롤백 필요
        public boolean requiresPointRollback() {
            return pointAmount.compareTo(BigDecimal.ZERO) > 0;
        }
        
        // 재고 복원은 항상 필요
        public boolean requiresStockRestore() {
            return true;
        }
    }

    /**
     * 환불 완료 이벤트
     */
    public record Refunded(
        Long orderId,
        String userId,
        String transactionKey,
        BigDecimal refundAmount,
        String reason,
        LocalDateTime refundedAt
    ) {

        public static Refunded from(Long orderId, String userId, String transactionKey, BigDecimal amount, String reason) {
            return new Refunded(orderId, userId, transactionKey, amount, reason, LocalDateTime.now());
        }
    }
}
