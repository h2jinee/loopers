package com.loopers.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserTest {
	/*
	 * 단위 테스트
		- [ ]  ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.
		- [ ]  이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.
		- [ ]  생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.
	 */
	@DisplayName("ID가 영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@ParameterizedTest
	@ValueSource(strings = {
		"h2jinee",
		"heejin",
		"hj",
		"heeeeeeeeeeeeeeeeeeeeeeeeeeeeejin"
	})
	void fail_whenIdFormatIsInvalid(String userId) {
		// arrange
		final String name = "전희진";

		// act
		// final User user = new User(
		// 	userId,
		// 	name
		// )

		// assert
	}

	@DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@Test
	void fail_whenEmailFormatIsInvalid() {
		// arrange

		// act

		// assert
	}

	@DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
	@Test
	void fail_whenBirthDateFormatIsInvalid() {
		// arrange

		// act

		// assert
	}
}
