package com.loopers.interfaces.api.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.loopers.interfaces.api.ApiResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {
	/**
	 * 회원가입 E2E 테스트
	 * - [x]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
	 * - [ ]  회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.
	 */

	@Autowired
	private TestRestTemplate testRestTemplate;

	@DisplayName("POST /api/v1/users")
	@Nested
	class Join {
		private final String ENDPOINT = "/api/v1/users";

		@DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
		@Test
		void returnsUserInfo_whenJoinIsSuccessful() {
			// arrange
			UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
				"h2jinee",
				"전희진",
				UserV1Dto.SignUpRequest.GenderRequest.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);

			// act
			ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
			};
			ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

			// assert
			assertAll(
				() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
				() -> assertThat(response.getBody()).isNotNull(),
				() -> assertThat(response.getBody().data().name()).isEqualTo(signUpRequest.name()),
				() -> assertThat(response.getBody().data().gender()).isEqualTo(UserV1Dto.GenderResponse.F)
			);

		}
		@DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
		@Test
		void returnsBadRequest_whenGenderIsMissing() {
			// arrange
			UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
				"h2jinee",
				"전희진",
				null,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);

			// act
			ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
			};
			ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(null), responseType);

			// assert
			assertAll(
				() -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
				() -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
				() -> assertThat(response.getBody()).isNull()
			);
		}
	}
}
