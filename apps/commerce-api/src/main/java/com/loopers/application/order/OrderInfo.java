package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderLineEntity;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.order.vo.ReceiverInfo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderInfo {
    
    public record CreateResult(
        Long orderId,
        BigDecimal totalAmount,
        OrderStatus status,
        ZonedDateTime paymentDeadline
    ) {
        public static CreateResult from(OrderEntity order) {
            return new CreateResult(
                order.getId(),
                order.getTotalAmount().amount(),
                order.getStatus(),
                order.getPaymentDeadline()
            );
        }
    }
    
    public record Detail(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        OrderStatus status,
        ReceiverInfo receiverInfo,
        List<OrderLineInfo> orderLines,
        ZonedDateTime paymentDeadline,
        ZonedDateTime orderedAt
    ) {
        public static Detail from(OrderEntity order) {
            List<OrderLineInfo> lines = order.getOrderLines().stream()
                .map(OrderLineInfo::from)
                .collect(Collectors.toList());
                
            return new Detail(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount().amount(),
                order.getStatus(),
                order.getReceiverInfo(),
                lines,
                order.getPaymentDeadline(),
                order.getCreatedAt()
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
        public static Summary from(OrderEntity order) {
            int itemCount = order.getOrderLines().size();
            String firstItemName = order.getOrderLines().isEmpty() ? "" 
                : order.getOrderLines().get(0).getProductName();
                
            if (itemCount > 1) {
                firstItemName += " 외 " + (itemCount - 1) + "건";
            }
            
            return new Summary(
                order.getId(),
                order.getTotalAmount().amount(),
                order.getStatus(),
                itemCount,
                firstItemName,
                order.getCreatedAt()
            );
        }
    }
    
    public record OrderLineInfo(
        Long orderLineId,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
    ) {
        public static OrderLineInfo from(OrderLineEntity line) {
            return new OrderLineInfo(
                line.getId(),
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getPrice().amount(),
                line.getSubtotal().amount()
            );
        }
    }
}
