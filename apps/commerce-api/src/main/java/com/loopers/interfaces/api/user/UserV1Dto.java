package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserValidator;

import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
	public record SignUpRequest(
		@NotNull
		String userId,
		@NotNull
		String name,
		@NotNull
		GenderRequest gender,
		@NotNull
		String birth,
		@NotNull
		String email
	) {
		public SignUpRequest {
			UserValidator.validateGender(gender);
			UserValidator.validateUserId(userId);
			UserValidator.validateEmail(email);
			UserValidator.validateBirth(birth);
		}
		
		public enum GenderRequest {
			M,
			F
		}
	}

	public record UserResponse(
		String userId,
		String name,
		GenderResponse gender,
		String birth,
		String email
	) { }

	public enum GenderResponse {
		M,
		F
	}
}
