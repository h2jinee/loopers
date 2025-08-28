package com.loopers.application.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PgPaymentInfo;

public class OrderCriteria {
    
    public record Create(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo,
        Money pointToUse,  // 사용할 포인트 (null 또는 0이면 미사용)
        PgPaymentInfo pgInfo  // PG 결제 정보 (포인트 전액 결제시 null 가능)
    ) {
        public OrderCommand.Create toOrderCommand() {
            return new OrderCommand.Create(userId, productId, quantity, receiverInfo);
        }
        
        public PaymentCommand.Point toPointPaymentCommand(Order order) {
            return new PaymentCommand.Point(
                order.getId(),
                userId,
                pointToUse
            );
        }
        
        public PaymentCommand.Pg toPgPaymentCommand(Order order, Money amount) {
            return new PaymentCommand.Pg(
                order.getId(),
                userId,
                amount,
                pgInfo
            );
        }
        
        public static Create withoutPoint(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            PgPaymentInfo pgInfo
        ) {
            return new Create(userId, productId, quantity, receiverInfo, null, pgInfo);
        }
        
        public static Create withPoint(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            Money pointToUse,
            PgPaymentInfo pgInfo
        ) {
            return new Create(userId, productId, quantity, receiverInfo, pointToUse, pgInfo);
        }
        
        public static Create pointOnly(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            Money pointToUse
        ) {
            return new Create(userId, productId, quantity, receiverInfo, pointToUse, null);
        }
    }
    
    public record GetDetail(
        String userId,
        Long orderId
    ) {
        public OrderCommand.GetDetail toCommand() {
            return new OrderCommand.GetDetail(userId, orderId);
        }
    }
    
    public record GetList(
        String userId,
        Integer page,
        Integer size
    ) {
        public OrderCommand.GetList toCommand() {
            return OrderCommand.GetList.of(userId, page, size);
        }
    }
    
    public record Cancel(
        String userId,
        Long orderId,
        String reason
    ) {
        public OrderCommand.Cancel toCommand() {
            return new OrderCommand.Cancel(userId, orderId, reason);
        }
    }
    
    public record UpdateStatus(
        Long orderId,
        String newStatus,
        String adminId
    ) {
        public OrderCommand.UpdateStatus toCommand() {
            return new OrderCommand.UpdateStatus(orderId, newStatus, adminId);
        }
    }
}
