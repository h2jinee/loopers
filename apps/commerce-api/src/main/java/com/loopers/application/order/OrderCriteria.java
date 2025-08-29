package com.loopers.application.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.payment.PaymentMethod;

public class OrderCriteria {
    
    public record Create(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo,
        Money pointToUse,  // 사용할 포인트 (null 또는 0이면 미사용)
        PaymentMethod paymentMethod  // 결제 방법 (POINT, PG, COMBINED)
    ) {
        public OrderCommand.Create toOrderCommand() {
            return new OrderCommand.Create(userId, productId, quantity, receiverInfo);
        }

        public static Create withoutPoint(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo
        ) {
            return new Create(userId, productId, quantity, receiverInfo, null, PaymentMethod.PG);
        }
        
        public static Create withPoint(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            Money pointToUse
        ) {
            return new Create(userId, productId, quantity, receiverInfo, pointToUse, PaymentMethod.COMBINED);
        }
        
        public static Create pointOnly(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            Money pointToUse
        ) {
            return new Create(userId, productId, quantity, receiverInfo, pointToUse, PaymentMethod.POINT);
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
}
