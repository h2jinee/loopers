package com.loopers.application.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.vo.ReceiverInfo;

public class OrderCriteria {

    public record Create(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo,
        Money pointAmount
    ) {
        // 포인트 없이 주문 (일반 결제)
        public static Create withoutPoint(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo
        ) {
            return new Create(userId, productId, quantity, receiverInfo, null);
        }

        // 포인트만으로 결제
        public static Create pointOnly(
            String userId,
            Long productId,
            Integer quantity,
            ReceiverInfo receiverInfo,
            Money pointAmount
        ) {
            return new Create(userId, productId, quantity, receiverInfo, pointAmount);
        }

        public OrderCommand.Create toOrderCommand() {
            return new OrderCommand.Create(userId, productId, quantity, receiverInfo);
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
