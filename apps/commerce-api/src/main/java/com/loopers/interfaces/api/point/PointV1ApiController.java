package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/point")
public class PointV1ApiController implements PointV1ApiSpec {

	private final PointService pointService;
	private final UserService userService;

	@GetMapping
	@Override
	public ApiResponse<PointV1Dto.PointResponse> getUserPoint(
		@RequestHeader(value = "X-USER-ID", required = false) String userId
	) {
		if (userId == null) {
			throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 없습니다.");
		}
		Long userPoint = pointService.getUserPoint(userId);
		return ApiResponse.success(new PointV1Dto.PointResponse(userId, userPoint));
	}

	@PostMapping
	@Override
	public ApiResponse<PointV1Dto.PointResponse> chargeUserPoint(
		PointV1Dto.PointRequest pointRequest
	) {
		UserEntity user = userService.getUserInfo(pointRequest.userId());

		if (user == null) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저입니다.");
		}

		PointEntity point = new PointEntity(
			pointRequest.userId(),
			pointRequest.point()
		);
		return ApiResponse.success(PointV1Dto.PointResponse.from(pointService.save(point)));
	}

}
