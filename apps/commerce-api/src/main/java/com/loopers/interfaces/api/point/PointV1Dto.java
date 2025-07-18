package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.validation.constraints.NotNull;

public class PointV1Dto {
	public record PointRequest(
		@NotNull
		String userId,
		Long point
	) {
		public PointRequest {
			if (userId == null) {
				throw new CoreException(ErrorType.BAD_REQUEST, "ID는 필수 값입니다.");
			}
			if (point == null) {
				throw new CoreException(ErrorType.BAD_REQUEST, "충전할 포인트 값을 입력해 주세요.");
			}
		}
	}

	public record PointResponse(
		String userId,
		Long point
	) {
		static PointResponse from(PointEntity pointEntity) {
			return new PointResponse(
				pointEntity.getUserId(),
				pointEntity.getPoint()
			);
		};
	}
}
