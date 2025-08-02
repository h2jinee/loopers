package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Order V1", description = "주문 관리 API")
public interface OrderV1ApiSpec {
    
    @Operation(summary = "주문 생성", description = "상품을 주문하고 포인트로 결제합니다.")
    ApiResponse<OrderDto.V1.Create.Response> createOrder(
        @Parameter(hidden = true) String userId,
        OrderDto.V1.Create.Request request
    );
    
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    ApiResponse<OrderDto.V1.GetDetail.Response> getOrderDetail(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "주문 ID", required = true) Long orderId
    );
    
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 목록을 조회합니다.")
    ApiResponse<Page<OrderDto.V1.GetList.Response>> getOrderList(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "페이지 번호 (0부터 시작)") Integer page,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") Integer size
    );
}
