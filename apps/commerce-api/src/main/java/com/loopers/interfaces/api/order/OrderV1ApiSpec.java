package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Order V1 API", description = "주문 관련 API")
public interface OrderV1ApiSpec {
    
    @Operation(summary = "주문 생성")
    ApiResponse<OrderDto.V1.Create.Response> createOrder(
        @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody OrderDto.V1.Create.Request request
    );
    
    @Operation(summary = "주문 상세 조회")
    ApiResponse<OrderDto.V1.GetDetail.Response> getOrderDetail(
        @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long orderId
    );
    
    @Operation(summary = "주문 목록 조회")
    ApiResponse<Page<OrderDto.V1.GetList.Response>> getOrderList(
        @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    );
}
