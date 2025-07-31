package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;
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
		// act & assert
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new UserId(userId);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
		assertThat(exception.getMessage()).isEqualTo("ID는 영문 및 숫자 10자 이내로 입력해야 합니다.");
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
		// act & assert
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new Email(email);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
		assertThat(exception.getMessage()).isEqualTo("이메일은 xx@yy.zz 형식이어야 합니다.");
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
		// act & assert
		final CoreException exception = assertThrows(CoreException.class, () -> {
			new Birth(birth);
		});

		// assert
		assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
		assertThat(exception.getMessage()).isEqualTo("생년월일은 yyyy-MM-dd 형식이어야 합니다.");
	}
}
