package com.loopers.interfaces.api.point;

import com.loopers.domain.user.UserValidator;

import jakarta.validation.constraints.NotNull;

public class PointV1Dto {
	public record PointRequest(
		@NotNull
		String userId
	) {
		public PointRequest {
			UserValidator.validateUserId(userId);
		}
	}

	public record PointResponse(
		String userId,
		Long point
	) {}
}
