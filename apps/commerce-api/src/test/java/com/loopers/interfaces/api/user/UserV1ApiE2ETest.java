package com.loopers.interfaces.api.user;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PointRepository pointRepository;

	@BeforeEach
	void setUp() {
		UserDto.V1.SignUp.Request signUpRequest = new UserDto.V1.SignUp.Request(
			"h2jinee",
			"전희진",
			UserEntity.Gender.F,
			"1997-01-18",
			"wjsgmlwls97@gmail.com"
		);
		testRestTemplate.postForEntity("/api/v1/users", signUpRequest, ApiResponse.class);
	}

	// TODO Repository 주입을 안 받게 할 수는 없을까?
	@AfterEach
	void tearDown() {
		userRepository.clear();
		pointRepository.clear();
	}

	/**
	 * 회원 가입 E2E 테스트
	 - [x]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
	 - [x]  회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.
	 */
	@DisplayName("POST /api/v1/users")
	@Nested
	class Join {
		private final String ENDPOINT = "/api/v1/users";

		@DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
		@Test
		void returnsUserInfo_whenJoinIsSuccessful() {
			// arrange
			UserDto.V1.SignUp.Request signUpRequest = new UserDto.V1.SignUp.Request(
				"devin",
				"김데빈",
				UserEntity.Gender.M,
				"2000-01-01",
				"devin@loopers.com"
			);

			// act
			ParameterizedTypeReference<ApiResponse<UserDto.V1.SignUp.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<UserDto.V1.SignUp.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

			ApiResponse<UserDto.V1.SignUp.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS);

			UserDto.V1.SignUp.Response data = body.data();
			assertThat(data).isNotNull();
			assertThat(data.userId()).isEqualTo(signUpRequest.userId());
			assertThat(data.name()).isEqualTo(signUpRequest.name());
			assertThat(data.gender()).isEqualTo(signUpRequest.gender());
			assertThat(data.birth()).isEqualTo(signUpRequest.birth());
			assertThat(data.email()).isEqualTo(signUpRequest.email());
		}

		@DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
		@Test
		void returnsBadRequest_whenGenderIsMissing() {
			// arrange
			String requestBody = """
				{
					"userId": "h2jinee",
					"name": "전희진",
					"birth": "1997-01-18",
					"email": "wjsgmlwls97@gmail.com"
				}
				""";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// act
			ParameterizedTypeReference<ApiResponse<UserDto.V1.SignUp.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<UserDto.V1.SignUp.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(requestBody, headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

			ApiResponse<UserDto.V1.SignUp.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
		}
	}

	/**
	 * 내 정보 조회 E2E 테스트
	 - [x]  내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
	 - [x]  존재하지 않는 ID로 조회할 경우, 404 Not Found 응답을 반환한다.
	*/
	@DisplayName("GET /api/v1/users/me")
	@Nested
	class GetUserInfo {
		private final String ENDPOINT = "/api/v1/users/me";

		@DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
		@Test
		void returnsUserInfo_whenUserExists() {
			// arrange
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-USER-ID", "h2jinee");

			// act
			ParameterizedTypeReference<ApiResponse<UserDto.V1.GetUser.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<UserDto.V1.GetUser.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

			ApiResponse<UserDto.V1.GetUser.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS);

			UserDto.V1.GetUser.Response data = body.data();
			assertThat(data).isNotNull();
			assertThat(data.userId()).isEqualTo("h2jinee");
			assertThat(data.name()).isEqualTo("전희진");
			assertThat(data.gender()).isEqualTo(UserEntity.Gender.F);
			assertThat(data.birth()).isEqualTo("1997-01-18");
			assertThat(data.email()).isEqualTo("wjsgmlwls97@gmail.com");
		}

		@DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
		@Test
		void returnsNotFound_whenUserIdDoesNotExist() {
			// arrange
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-USER-ID", "devin");

			// act
			ParameterizedTypeReference<ApiResponse<UserDto.V1.GetUser.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<UserDto.V1.GetUser.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

			ApiResponse<UserDto.V1.GetUser.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
		}
	}
}
