package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

@RestController
@RequestMapping("/api/v1/point")
public class PointV1ApiController implements PointV1ApiSpec {

	@GetMapping
	@Override
	public ApiResponse<PointV1Dto.PointResponse> getUserPoint(
		@RequestHeader(value = "X-USER-ID", required = false) String userId
	) {
		if (userId == null) {
			throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 없습니다.");
		}
		return ApiResponse.success(new PointV1Dto.PointResponse(userId, 0L));
	}
}
