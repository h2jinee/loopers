package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;

@SpringBootTest
public class PointServiceIntegrationTest {

	@Autowired
	private PointService pointService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PointRepository pointRepository;

	@AfterEach
	void tearDown() {
		userRepository.clear();
		pointRepository.clear();
	}

	@BeforeEach
	void setUp() {
		UserEntity user = new UserEntity(
			"h2jinee",
			"전희진",
			UserEntity.Gender.F,
			"1997-01-18",
			"wjsgmlwls97@gmail.com"
		);
		userService.save(user);
	}

	/*
	* 포인트 조회 통합 테스트
	- [x]  해당 ID의 회원이 존재할 경우, 보유 포인트가 반환된다.
	- [x]  해당 ID의 회원이 존재하지 않을 경우, null이 반환된다.
	*/
	@DisplayName("포인트 조회 시")
	@Nested
	class getUserPoint {
		@DisplayName("해당 ID의 회원이 존재할 경우, 보유 포인트가 반환된다.")
		@Test
		void returnUserPoint_whenUserIdExists() {
			// arrange
			String userId = "h2jinee";

			// act
			Long point = pointService.getUserPoint(userId);

			// assert
			assertThat(point).isNotNull();
			assertThat(point).isEqualTo(0L);
		}

		@DisplayName("해당 ID의 회원이 존재하지 않을 경우, null이 반환된다.")
		@Test
		void returnsNull_whenUserIdDoesNotExist() {
			// arrange
			String userId = "devin";

			// act
			Long point = pointService.getUserPoint(userId);

			// assert
			assertThat(point).isNull();
		}
	}
}
