package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PointTest {

	/*
	 * 포인트 충전 단위 테스트
		- [x]  0 이하의 정수로 포인트를 충전 시 실패한다.
	 */
	@DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
	@ParameterizedTest
	@ValueSource(longs = {
		-1,
		-1000000000,
		-2000,
		0
	})
	void fail_whenPointIsNotPositive(Long point) {
		// arrange
		final String userId = "h2jinee";

		// act
		final CoreException exception = assertThrows(CoreException.class, () -> {
			PointEntity.charge(userId, point);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
		assertThat(exception.getMessage()).isEqualTo("충전 금액은 0보다 커야 합니다.");
	}
}
