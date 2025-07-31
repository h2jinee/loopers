package com.loopers.interfaces.api.point;

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
import org.springframework.http.ResponseEntity;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

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
	 * 포인트 조회 E2E 테스트
	 - [x]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
	 - [x]  X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.
	 */
	@DisplayName("GET /api/v1/point")
	@Nested
	class getUserPoint {
		private final String ENDPOINT = "/api/v1/point";

		@DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
		@Test
		void returnsPointInfo_whenUserExists() {
			// arrange
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-USER-ID", "h2jinee");

			// act
			ParameterizedTypeReference<ApiResponse<PointDto.V1.GetPoint.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointDto.V1.GetPoint.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

			ApiResponse<PointDto.V1.GetPoint.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS);

			PointDto.V1.GetPoint.Response data = body.data();
			assertThat(data.point()).isEqualTo(0L);
		}

		@DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
		@Test
		void returnsBadRequest_whenUserIdHeaderIsMissing() {
			// arrange
			HttpHeaders headers = new HttpHeaders();

			// act
			ParameterizedTypeReference<ApiResponse<PointDto.V1.GetPoint.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointDto.V1.GetPoint.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

			ApiResponse<PointDto.V1.GetPoint.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
		}
	}

	/**
	 * 포인트 충전 E2E 테스트
	 - [x]  존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
	 - [x]  존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.
	 */
	@DisplayName("POST /api/v1/point")
	@Nested
	class chargeUserPoint {
		private final String ENDPOINT = "/api/v1/point";

		@DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
		@Test
		void returnsPointBalance_whenUserChargesPoint() {
			// arrange
			PointDto.V1.Charge.Request pointRequest = new PointDto.V1.Charge.Request(
				"h2jinee",
				1000L
			);

			// act
			ParameterizedTypeReference<ApiResponse<PointDto.V1.Charge.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointDto.V1.Charge.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(pointRequest), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

			ApiResponse<PointDto.V1.Charge.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS);

			PointDto.V1.Charge.Response data = body.data();
			assertThat(data.totalPoint()).isEqualTo(1000L);
		}

		@DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
		@Test
		void returnsBadRequest_whenUserDoesNotExist() {
			// arrange
			PointDto.V1.Charge.Request pointRequest = new PointDto.V1.Charge.Request(
				"alen",
				1000L
			);

			// act
			ParameterizedTypeReference<ApiResponse<PointDto.V1.Charge.Response>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointDto.V1.Charge.Response>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(pointRequest), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

			ApiResponse<PointDto.V1.Charge.Response> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
		}
	}
}
