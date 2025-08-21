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
        
        public static Create from(
            String userId,
            Long productId,
            Integer quantity,
            String receiverName,
            String receiverPhone,
            String receiverZipCode,
            String receiverAddress,
            String receiverAddressDetail
        ) {
            ReceiverInfo receiverInfo = new ReceiverInfo(
                receiverName,
                receiverPhone,
                receiverZipCode,
                receiverAddress,
                receiverAddressDetail
            );
            return new Create(userId, productId, quantity, receiverInfo);
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
