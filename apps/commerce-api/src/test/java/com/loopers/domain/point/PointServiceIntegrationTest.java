package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.loopers.domain.point.vo.ChargePoint;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

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

	@BeforeEach
	void setUp() {
		UserCommand.Create command = new UserCommand.Create(
			new UserId("h2jinee"),
			"전희진",
			UserEntity.Gender.F,
			new Birth("1997-01-18"),
			new Email("wjsgmlwls97@gmail.com")
		);
		UserEntity user = userService.createUser(command);
		// 포인트 초기화
		pointService.initializeUserPoint(user.getUserId());
	}

	@AfterEach
	void tearDown() {
		userRepository.clear();
		pointRepository.clear();
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
			assertThat(point).isEqualTo(0L);
		}
	}

	/*
	* 포인트 충전 통합 테스트
	- [x]  존재하지 않는 유저 ID로 충전을 시도한 경우, 실패한다.
	*/
	@DisplayName("포인트 충전 시")
	@Nested
	class chargeUserPoint {
		@DisplayName("존재하지 않는 유저 ID로 충전을 시도한 경우, 실패한다.")
		@Test
		void fail_whenUserDoesNotExist() {
			// arrange
			String userId = "devin";
			PointCommand.Charge command = new PointCommand.Charge(
				userId,
				new ChargePoint(1000L)
			);

			// act & assert
			CoreException exception = assertThrows(CoreException.class, () -> {
				pointService.charge(command);
			});

			// assert
			assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
			assertThat(exception.getMessage()).isEqualTo("존재하지 않는 사용자입니다.");
		}
	}
}
