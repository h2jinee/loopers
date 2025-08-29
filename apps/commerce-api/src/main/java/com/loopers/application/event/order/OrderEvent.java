package com.loopers.application.event.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.payment.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 주문 도메인 이벤트
 */
public class OrderEvent {
    
    /**
     * 주문 생성 이벤트
     */
    public record Created(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        BigDecimal pointAmount,      // 포인트 사용 요청액
        BigDecimal pgAmount,          // PG 결제 필요액
        List<OrderLineSnapshot> orderLines,
        ReceiverInfo receiverInfo,
        PaymentMethod paymentMethod,
        ZonedDateTime createdAt
    ) {
        
        public static Created from(Order order, Money pointToUse, PaymentMethod paymentMethod) {
            BigDecimal pointAmount = pointToUse != null ? pointToUse.amount() : BigDecimal.ZERO;
            BigDecimal totalAmount = order.getTotalAmount().amount();
            
            // paymentMethod가 명시적으로 전달되면 사용, 아니면 자동 결정
            PaymentMethod method = paymentMethod != null ? paymentMethod :
                determinePaymentMethod(pointAmount, totalAmount);
            
            return new Created(
                order.getId(),
                order.getUserId(),
                totalAmount,
                pointAmount,
                totalAmount.subtract(pointAmount),
                order.getOrderLines().stream()
                    .map(OrderLineSnapshot::from)
                    .toList(),
                order.getReceiverInfo(),
                method,
                order.getCreatedAt()
            );
        }
        
        // 기존 메서드 유지 (하위 호환성)
        public static Created from(Order order, Money pointToUse) {
            return from(order, pointToUse, null);
        }
        
        private static PaymentMethod determinePaymentMethod(BigDecimal pointAmount, BigDecimal totalAmount) {
            if (pointAmount.compareTo(BigDecimal.ZERO) == 0) {
                return PaymentMethod.PG;
            } else if (pointAmount.compareTo(totalAmount) == 0) {
                return PaymentMethod.POINT;
            } else {
                return PaymentMethod.COMBINED;
            }
        }
    }
    
    /**
     * 주문 확정 이벤트
     */
    public record Confirmed(
        Long orderId,
        String userId,
        LocalDateTime confirmedAt
    ) {
        
        public static Confirmed from(Long orderId, String userId) {
            return new Confirmed(orderId, userId, LocalDateTime.now());
        }
    }
    
    /**
     * 주문 실패 이벤트
     */
    public record Failed(
        Long orderId,
        String userId,
        String reason,
        LocalDateTime failedAt
    ) {
        
        public static Failed from(Long orderId, String userId, String reason) {
            return new Failed(orderId, userId, reason, LocalDateTime.now());
        }
    }
}
