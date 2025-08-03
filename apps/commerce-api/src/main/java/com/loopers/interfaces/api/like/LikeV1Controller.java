package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeApplicationService;
import com.loopers.application.like.LikeInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like/products")
public class LikeV1Controller implements LikeV1ApiSpec {
    
    private final LikeApplicationService likeApplicationService;
    
    @PostMapping("/{productId}")
    @Override
    public ApiResponse<LikeDto.V1.ToggleLike.Response> addLike(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long productId
    ) {
        validateUserId(userId);
        LikeInfo.Result result = likeApplicationService.addLike(userId, productId);
        return ApiResponse.success(LikeDto.V1.ToggleLike.Response.from(result));
    }
    
    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<LikeDto.V1.ToggleLike.Response> removeLike(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long productId
    ) {
        validateUserId(userId);
        LikeInfo.Result result = likeApplicationService.removeLike(userId, productId);
        return ApiResponse.success(LikeDto.V1.ToggleLike.Response.from(result));
    }
    
    @GetMapping
    @Override
    public ApiResponse<Page<LikeDto.V1.GetLikedProducts.Response>> getLikedProducts(
        @RequestHeader("X-USER-ID") String userId,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        validateUserId(userId);
        Page<LikeInfo.LikedProduct> likedProducts = likeApplicationService.getLikedProducts(userId, page, size);
        Page<LikeDto.V1.GetLikedProducts.Response> response = likedProducts.map(LikeDto.V1.GetLikedProducts.Response::from);
        return ApiResponse.success(response);
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
    }
}
