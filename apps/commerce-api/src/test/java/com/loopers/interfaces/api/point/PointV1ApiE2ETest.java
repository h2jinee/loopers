package com.loopers.interfaces.api.point;

import static org.assertj.core.api.Assertions.*;

import java.util.Random;
import java.util.UUID;

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

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@BeforeEach
	void setUp() {
		UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
			"h2jinee" + new Random().nextInt(999),
			"전희진",
			UserV1Dto.SignUpRequest.GenderRequest.F,
			"1997-01-18",
			"wjsgmlwls97@gmail.com"
		);
		testRestTemplate.postForEntity("/api/v1/users", signUpRequest, ApiResponse.class);
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
			ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

			ApiResponse<PointV1Dto.PointResponse> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS);

			PointV1Dto.PointResponse data = body.data();
			assertThat(data.point()).isEqualTo(0L);
		}

		@DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
		@Test
		void returnsBadRequest_whenUserIdHeaderIsMissing() {
			// arrange
			HttpHeaders headers = new HttpHeaders();

			// act
			ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
			ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
				testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), responseType);

			// assert
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

			ApiResponse<PointV1Dto.PointResponse> body = response.getBody();
			assertThat(body).isNotNull();
			assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);

		}
	}
}
