package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Brand V1", description = "브랜드 관리 API")
public interface BrandV1ApiSpec {
    
    @Operation(summary = "브랜드 목록 조회", description = "브랜드 목록을 페이지네이션으로 조회합니다.")
    ApiResponse<Page<BrandDto.V1.GetList.Response>> getBrandList(
        @Parameter(description = "페이지 번호 (0부터 시작)") Integer page,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") Integer size
    );
    
    @Operation(summary = "브랜드 상세 조회", description = "특정 브랜드의 상세 정보를 조회합니다.")
    ApiResponse<BrandDto.V1.GetDetail.Response> getBrandDetail(
        @Parameter(description = "브랜드 ID", required = true) Long brandId
    );
}
