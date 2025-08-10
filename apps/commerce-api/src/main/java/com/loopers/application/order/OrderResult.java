package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.order.vo.ReceiverInfo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class OrderResult {
    
    public record CreateResult(
        Long orderId,
        BigDecimal totalAmount,
        OrderStatus status,
        ZonedDateTime paymentDeadline
    ) {
        public static CreateResult from(OrderInfo.CreateResult domainInfo) {
            return new CreateResult(
                domainInfo.orderId(),
                domainInfo.totalAmount(),
                domainInfo.status(),
                domainInfo.paymentDeadline()
            );
        }
    }
    
    public record Detail(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        OrderStatus status,
        ReceiverInfo receiverInfo,
        List<OrderLineResult> orderLines,
        ZonedDateTime paymentDeadline,
        ZonedDateTime orderedAt
    ) {
        public static Detail from(OrderInfo.Detail domainInfo) {
            List<OrderLineResult> lines = domainInfo.orderLines().stream()
                .map(OrderLineResult::from)
                .toList();
                
            return new Detail(
                domainInfo.orderId(),
                domainInfo.userId(),
                domainInfo.totalAmount(),
                domainInfo.status(),
                domainInfo.receiverInfo(),
                lines,
                domainInfo.paymentDeadline(),
                domainInfo.orderedAt()
            );
        }
    }
    
    public record Summary(
        Long orderId,
        BigDecimal totalAmount,
        OrderStatus status,
        Integer itemCount,
        String firstItemName,
        ZonedDateTime orderedAt
    ) {
        public static Summary from(OrderInfo.Summary domainInfo) {
            return new Summary(
                domainInfo.orderId(),
                domainInfo.totalAmount(),
                domainInfo.status(),
                domainInfo.itemCount(),
                domainInfo.firstItemName(),
                domainInfo.orderedAt()
            );
        }
    }
    
    public record OrderLineResult(
        Long orderLineId,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
    ) {
        public static OrderLineResult from(OrderInfo.OrderLineInfo domainInfo) {
            return new OrderLineResult(
                domainInfo.orderLineId(),
                domainInfo.productId(),
                domainInfo.productName(),
                domainInfo.quantity(),
                domainInfo.price(),
                domainInfo.subtotal()
            );
        }
    }
}