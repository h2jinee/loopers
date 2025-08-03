package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderApplicationService;
import com.loopers.application.order.OrderInfo;
import com.loopers.domain.order.OrderCommand;
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
    
    private final OrderApplicationService orderApplicationService;
    
    @PostMapping
    @Override
    public ApiResponse<OrderDto.V1.Create.Response> createOrder(
        @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody OrderDto.V1.Create.Request request
    ) {
        validateUserId(userId);
        
        OrderCommand.Create command = new OrderCommand.Create(
            userId,
            request.productId(),
            request.quantity(),
            request.toReceiverInfo()
        );
        
        OrderInfo.CreateResult result = orderApplicationService.createOrder(command);
        return ApiResponse.success(OrderDto.V1.Create.Response.from(result));
    }
    
    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderDto.V1.GetDetail.Response> getOrderDetail(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long orderId
    ) {
        validateUserId(userId);
        
        OrderInfo.Detail detail = orderApplicationService.getOrderDetail(orderId, userId);
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
        
        Page<OrderInfo.Summary> orders = orderApplicationService.getUserOrders(userId, page, size);
        Page<OrderDto.V1.GetList.Response> response = orders.map(OrderDto.V1.GetList.Response::from);
        
        return ApiResponse.success(response);
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
    }
}
