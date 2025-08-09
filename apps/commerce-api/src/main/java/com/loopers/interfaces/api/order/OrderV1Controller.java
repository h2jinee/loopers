package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {
    
    private final OrderFacade orderFacade;
    
    @PostMapping
    @Override
    public ApiResponse<OrderDto.V1.Create.Response> createOrder(
        @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody OrderDto.V1.Create.Request request
    ) {
        validateUserId(userId);
        
        OrderCriteria.Create criteria = new OrderCriteria.Create(
            userId,
            request.productId(),
            request.quantity(),
            request.toReceiverInfo()
        );
        
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);
        return ApiResponse.success(OrderDto.V1.Create.Response.from(result));
    }
    
    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderDto.V1.GetDetail.Response> getOrderDetail(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long orderId
    ) {
        validateUserId(userId);
        
        OrderCriteria.GetDetail criteria = new OrderCriteria.GetDetail(userId, orderId);
        OrderResult.Detail detail = orderFacade.getOrderDetail(criteria);
        return ApiResponse.success(OrderDto.V1.GetDetail.Response.from(detail));
    }
    
    @GetMapping
    @Override
    public ApiResponse<Page<OrderDto.V1.GetList.Response>> getOrderList(
        @RequestHeader("X-USER-ID") String userId,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        validateUserId(userId);
        
        OrderCriteria.GetList criteria = new OrderCriteria.GetList(userId, page, size);
        Page<OrderResult.Summary> orders = orderFacade.getUserOrders(criteria);
        Page<OrderDto.V1.GetList.Response> response = orders.map(OrderDto.V1.GetList.Response::from);
        
        return ApiResponse.success(response);
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
    }
}
