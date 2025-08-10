package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.vo.ReceiverInfo;

public class OrderCriteria {
    
    public record Create(
        String userId,
        Long productId,
        Integer quantity,
        ReceiverInfo receiverInfo
    ) {
        public OrderCommand.Create toCommand() {
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