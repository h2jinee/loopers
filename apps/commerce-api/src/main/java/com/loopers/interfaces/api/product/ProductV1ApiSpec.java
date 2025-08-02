package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Product V1", description = "상품 관리 API")
public interface ProductV1ApiSpec {
    
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다. 브랜드별 필터링과 정렬을 지원합니다.")
    ApiResponse<Page<ProductDto.V1.GetList.Response>> getProductList(
        @Parameter(description = "브랜드 ID (선택사항)") Long brandId,
        @Parameter(description = "정렬 방식 (latest, price_asc, likes_desc)") String sort,
        @Parameter(description = "페이지 번호 (0부터 시작)") Integer page,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") Integer size
    );
    
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    ApiResponse<ProductDto.V1.GetDetail.Response> getProductDetail(
        @Parameter(description = "상품 ID", required = true) Long productId
    );
}
