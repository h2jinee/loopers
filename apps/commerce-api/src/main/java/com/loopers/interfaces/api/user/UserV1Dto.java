package com.loopers.interfaces.api.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

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
			if (gender == null) {
				throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 값입니다.");
			}
		}
		enum GenderRequest {
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

	enum GenderResponse {
		M,
		F
	}
}
