package com.loopers.application.order;

import com.loopers.application.event.order.OrderLineSnapshot;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.payment.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 완료 이벤트
 * 멘토님 언급: orderId만 주지 말고 스냅샷 포함하여 다른 도메인이 독립적으로 판단 가능하게
 */
public record OrderCompleted(
    Long orderId,
    String userId,
    Long productId,
    Integer quantity,
    BigDecimal totalAmount,
    BigDecimal discountAmount,  // 쿠폰 할인 정보 (스냅샷)
    BigDecimal pointAmount,     // 포인트 사용 정보 (스냅샷)
    PaymentMethod paymentMethod,
    List<OrderLineSnapshot> orderLines, // 상세 스냅샷
    LocalDateTime completedAt
) {
    
    /**
     * 주문과 기준 정보로부터 이벤트 생성
     */
    public static OrderCompleted from(Order order, OrderCriteria.Create criteria) {
        return new OrderCompleted(
            order.getId(),
            order.getUserId(),
            criteria.productId(),
            criteria.quantity(),
            order.getTotalAmount().amount(),
            // 스냅샷: 쿠폰 할인 금액 (실제로는 외부에서 계산되어 전달)
            extractDiscountAmount(criteria),
            // 스냅샷: 포인트 사용 금액
            criteria.pointToUse() != null ? criteria.pointToUse().amount() : BigDecimal.ZERO,
            criteria.paymentMethod(),
            // 스냅샷: 주문 라인 정보
            order.getOrderLines().stream()
                .map(OrderCompleted::createOrderLineSnapshot)
                .toList(),
            LocalDateTime.now()
        );
    }
    
    private static BigDecimal extractDiscountAmount(OrderCriteria.Create criteria) {
        // TODO: criteria에 discountAmount 추가 필요
        return BigDecimal.ZERO;
    }
    
    private static OrderLineSnapshot createOrderLineSnapshot(OrderLine orderLine) {
        return new OrderLineSnapshot(
            orderLine.getProductId(),
            orderLine.getProductName(),
            orderLine.getQuantity(),
            orderLine.getPrice().amount(),
            orderLine.getPrice().amount().multiply(BigDecimal.valueOf(orderLine.getQuantity()))
        );
    }
    
    /**
     * 재고 차감이 필요한지 판단
     */
    public boolean requiresStockDeduction() {
        return quantity > 0;
    }
    
    /**
     * 쿠폰 사용이 필요한지 판단
     */
    public boolean requiresCouponUsage() {
        return discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 포인트 사용이 필요한지 판단
     */
    public boolean requiresPointUsage() {
        return pointAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 결제가 필요한지 판단
     */
    public boolean requiresPayment() {
        BigDecimal paymentAmount = totalAmount
            .subtract(discountAmount)
            .subtract(pointAmount);
        return paymentAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}