package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class UserTest {
	/*
	 * 회원 가입 단위 테스트
		- [x]  ID가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.
		- [x]  이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.
		- [x]  생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.
	 */
	@DisplayName("ID가 영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@ParameterizedTest
	@ValueSource(strings = {
		"h2jinee_",
		"heejin22222222222222",
		"hj1234567890",
		"heeeeeeeeeeeeeeeeeeeeeeeeeeeeejin",
		""
	})
	void fail_whenIdFormatIsInvalid(String userId) {
		// arrange
		final String name = "전희진";
		final String email = "wjsgmlwls97@gmail.com";
		final String birth = "1997-01-18";
		final UserEntity.Gender gender = UserEntity.Gender.F;

		// act
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new UserEntity(
				userId,
				name,
				gender,
				birth,
				email
			);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
	}

	@DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"wjsgmlwls97",
		"wjsgmlwls97@gmail",
		"wjsgmlwls97@gmail.."
	})
	void fail_whenEmailFormatIsInvalid(String email) {
		// arrange
		final String userId = "h2jinee";
		final String name = "전희진";
		final String birth = "1997-01-18";
		final UserEntity.Gender gender = UserEntity.Gender.F;

		// act
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new UserEntity(
				userId,
				name,
				gender,
				birth,
				email
			);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
	}

	@DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@ParameterizedTest
	@ValueSource(strings = {
		"1800.01.18",
		"2000...",
		"01.18",
		"2000-01-383",
		"YYYY-MM-DD",
		""
	})
	void fail_whenBirthDateFormatIsInvalid(String birth) {
		// arrange
		final String userId = "h2jinee";
		final String name = "전희진";
		final String email = "wjsgmlwls97@gmail.com";
		final UserEntity.Gender gender = UserEntity.Gender.F;

		// act
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new UserEntity(
				userId,
				name,
				gender,
				birth,
				email
			);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
	}
}
