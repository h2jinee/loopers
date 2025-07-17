package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1ApiController implements UserV1ApiSpec {

	private final UserService userService;

	@PostMapping
	@Override
	public ApiResponse<UserV1Dto.UserResponse> signUp(
		@RequestBody UserV1Dto.SignUpRequest signUpRequest
	) {
		UserEntity user = new UserEntity(
			signUpRequest.userId(),
			signUpRequest.name(),
			signUpRequest.gender() == UserV1Dto.SignUpRequest.GenderRequest.M
				? UserEntity.Gender.M
				: UserEntity.Gender.F,
			signUpRequest.birth(),
			signUpRequest.email()

		);
		return ApiResponse.success(UserV1Dto.UserResponse.from(userService.save(user)));
	}

	@GetMapping("{userId}")
	@Override
	public ApiResponse<UserV1Dto.UserResponse> getUserInfo(
		@PathVariable String userId
	) {
		UserEntity user = userService.findByUserId(userId);
		if (user == null) {
			throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다.");
		}
		return ApiResponse.success(UserV1Dto.UserResponse.from(user));
	}
}
