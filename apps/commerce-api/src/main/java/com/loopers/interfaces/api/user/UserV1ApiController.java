package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.interfaces.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserV1ApiController implements UserV1ApiSpec {

	@PostMapping
	@Override
	public ApiResponse<UserV1Dto.UserResponse> signUp(
		@RequestBody UserV1Dto.SignUpRequest signUpRequest
	) {
		return ApiResponse.success(
			new UserV1Dto.UserResponse(
				"h2jinee",
				"전희진",
				UserV1Dto.GenderResponse.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			)
		);
	}
}
