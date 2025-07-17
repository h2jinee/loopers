package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.RequestHeader;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Point V1 API", description = "포인트 API 입니다.")
public interface PointV1ApiSpec {

	@Operation(summary = "포인트 조회")
	ApiResponse<PointV1Dto.PointResponse> getUserPoint(
		@RequestHeader("X-USER-ID") String userId
	);
}
