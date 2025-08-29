package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.order.vo.ReceiverInfo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDto {
    public static class V1 {
        
        public static class Create {
            public record Request(
                @NotNull(message = "상품 ID는 필수입니다.")
                Long productId,
                
                @NotNull(message = "수량은 필수입니다.")
                @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
                Integer quantity,
                
                @NotBlank(message = "수령인 이름은 필수입니다.")
                String receiverName,
                
                @NotBlank(message = "수령인 전화번호는 필수입니다.")
                String receiverPhone,
                
                @NotBlank(message = "우편번호는 필수입니다.")
                String receiverZipCode,
                
                @NotBlank(message = "주소는 필수입니다.")
                String receiverAddress,
                
                String receiverAddressDetail,
                
                BigDecimal pointToUse,
                String cardType,
                String cardNo
            ) {
                
                public OrderCriteria.Create toCriteria(String userId) {
                    ReceiverInfo receiverInfo = new ReceiverInfo(
                        receiverName, receiverPhone, receiverZipCode, 
                        receiverAddress, receiverAddressDetail
                    );
                    
                    boolean hasPointToUse = pointToUse != null && pointToUse.compareTo(BigDecimal.ZERO) > 0;
                    boolean hasPgInfo = cardType != null && cardNo != null;
                    
                    if (hasPointToUse) {
                        Money pointMoney = Money.of(pointToUse);
                        
                        if (hasPgInfo) {
                            return OrderCriteria.Create.withPoint(
                                userId, productId, quantity, receiverInfo, pointMoney
                            );
                        } else {
                            return OrderCriteria.Create.pointOnly(
                                userId, productId, quantity, receiverInfo, pointMoney
                            );
                        }
                    } else {
                        if (!hasPgInfo) {
                            throw new IllegalArgumentException("결제 정보가 필요합니다.");
                        }
                        return OrderCriteria.Create.withoutPoint(
                            userId, productId, quantity, receiverInfo
                        );
                    }
                }
            }
            
            public record Response(
                Long orderId,
                BigDecimal totalAmount,
                OrderStatus status,
                ZonedDateTime paymentDeadline
            ) {
                public static Response from(OrderResult.CreateResult result) {
                    return new Response(
                        result.orderId(),
                        result.totalAmount(),
                        result.status(),
                        result.paymentDeadline()
                    );
                }
            }
        }
        
        public static class GetDetail {
            public record Response(
                Long orderId,
                String userId,
                BigDecimal totalAmount,
                OrderStatus status,
                ReceiverResponse receiver,
                List<OrderLineResponse> orderLines,
                ZonedDateTime paymentDeadline,
                ZonedDateTime orderedAt
            ) {
                public static Response from(OrderResult.Detail detail) {
                    List<OrderLineResponse> lines = detail.orderLines().stream()
                        .map(OrderLineResponse::from)
                        .collect(Collectors.toList());
                        
                    return new Response(
                        detail.orderId(),
                        detail.userId(),
                        detail.totalAmount(),
                        detail.status(),
                        ReceiverResponse.from(detail.receiverInfo()),
                        lines,
                        detail.paymentDeadline(),
                        detail.orderedAt()
                    );
                }
            }
            
            public record ReceiverResponse(
                String name,
                String phone,
                String zipCode,
                String address,
                String addressDetail
            ) {
                public static ReceiverResponse from(ReceiverInfo info) {
                    return new ReceiverResponse(
                        info.getName(),
                        info.getPhone(),
                        info.getZipCode(),
                        info.getAddress(),
                        info.getAddressDetail()
                    );
                }
            }
            
            public record OrderLineResponse(
                Long orderLineId,
                Long productId,
                String productName,
                Integer quantity,
                BigDecimal price,
                BigDecimal subtotal
            ) {
                public static OrderLineResponse from(OrderResult.OrderLineResult line) {
                    return new OrderLineResponse(
                        line.orderLineId(),
                        line.productId(),
                        line.productName(),
                        line.quantity(),
                        line.price(),
                        line.subtotal()
                    );
                }
            }
        }
        
        public static class GetList {
            public record Response(
                Long orderId,
                BigDecimal totalAmount,
                OrderStatus status,
                Integer itemCount,
                String firstItemName,
                ZonedDateTime orderedAt
            ) {
                public static Response from(OrderResult.Summary summary) {
                    return new Response(
                        summary.orderId(),
                        summary.totalAmount(),
                        summary.status(),
                        summary.itemCount(),
                        summary.firstItemName(),
                        summary.orderedAt()
                    );
                }
            }
        }
    }
}
