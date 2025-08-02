package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Like V1", description = "좋아요 관리 API")
public interface LikeV1ApiSpec {
    
    @Operation(summary = "상품 좋아요 등록", description = "상품에 좋아요를 등록합니다. 이미 좋아요가 있어도 에러가 발생하지 않습니다.")
    ApiResponse<LikeDto.V1.ToggleLike.Response> addLike(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "상품 ID", required = true) Long productId
    );
    
    @Operation(summary = "상품 좋아요 취소", description = "상품의 좋아요를 취소합니다. 좋아요가 없어도 에러가 발생하지 않습니다.")
    ApiResponse<LikeDto.V1.ToggleLike.Response> removeLike(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "상품 ID", required = true) Long productId
    );
    
    @Operation(summary = "좋아요한 상품 목록 조회", description = "사용자가 좋아요한 상품 목록을 조회합니다.")
    ApiResponse<Page<LikeDto.V1.GetLikedProducts.Response>> getLikedProducts(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "페이지 번호 (0부터 시작)") Integer page,
        @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") Integer size
    );
}
