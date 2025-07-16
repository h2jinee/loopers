package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "사용자 API 입니다.")
public interface UserV1ApiSpec {

	@Operation(summary = "회원가입")
	ApiResponse<UserV1Dto.UserResponse> signUp(
		UserV1Dto.SignUpRequest signUpRequest
	);
}
