package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {
    
    private final OrderFacade orderFacade;
    
    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ApiResponse<OrderDto.V1.Create.Response> createOrder(
        @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody OrderDto.V1.Create.Request request
    ) {
        validateUserId(userId);
        
        OrderCriteria.Create criteria = OrderCriteria.Create.from(
            userId,
            request.productId(),
            request.quantity(),
            request.receiverName(),
            request.receiverPhone(),
            request.receiverZipCode(),
            request.receiverAddress(),
            request.receiverAddressDetail()
        );
        
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);
        
        return ApiResponse.success(OrderDto.V1.Create.Response.from(result));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
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
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 목록을 조회합니다.")
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
