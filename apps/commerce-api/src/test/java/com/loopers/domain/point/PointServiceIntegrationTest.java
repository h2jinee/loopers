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

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointCriteria;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.point.PointHistoryJpaRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;

@SpringBootTest
public class PointServiceIntegrationTest {

	@Autowired
	private PointFacade pointFacade;

	@Autowired
	private UserService userService;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private PointJpaRepository pointJpaRepository;
	
	@Autowired
	private PointHistoryJpaRepository pointHistoryJpaRepository;

	@BeforeEach
	void setUp() {
		UserCommand.Create command = new UserCommand.Create(
			new UserId("h2jinee"),
			"전희진",
			User.Gender.F,
			new Birth("1997-01-18"),
			new Email("wjsgmlwls97@gmail.com")
		);
		User user = userService.createUser(command);
		// 포인트 초기화
		pointFacade.initializeUserPoint(user.getUserId());
	}

	@AfterEach
	void tearDown() {
		pointHistoryJpaRepository.deleteAll();
		pointJpaRepository.deleteAll();
		userJpaRepository.deleteAll();
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
			Long point = pointJpaRepository.findByUserId(userId)
				.map(p -> p.getBalance().amount().longValue())
				.orElse(null);

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
			Long point = pointJpaRepository.findByUserId(userId)
				.map(p -> p.getBalance().amount().longValue())
				.orElse(null);

			// assert
			assertThat(point).isNull();
		}
	}

	/*
	* 포인트 충전 통합 테스트
	- [x]  존재하는 유저 ID로 충전을 시도한 경우, 성공한다.
	- [x]  존재하지 않는 유저 ID로 충전을 시도한 경우, 실패한다.
	*/
	@DisplayName("포인트 충전 시")
	@Nested
	class charge {
		@DisplayName("존재하는 유저 ID로 충전을 시도한 경우, 성공한다.")
		@Test
		void success_whenUserExists() {
			// arrange
			String userId = "h2jinee";
			PointCriteria.Charge criteria = new PointCriteria.Charge(userId, 1000L);

			// act
			var result = pointFacade.charge(criteria);

			// assert
			assertThat(result).isNotNull();
			assertThat(result.userId()).isEqualTo(userId);
			assertThat(result.balance()).isEqualTo(1000L);
			
			// DB 확인
			var point = pointJpaRepository.findByUserId(userId).orElse(null);
			assertThat(point).isNotNull();
			assertThat(point.getBalance().amount().longValue()).isEqualTo(1000L);
		}
		
		@DisplayName("포인트가 이미 있는 유저가 충전하면 누적된다.")
		@Test
		void accumulates_whenUserAlreadyHasPoints() {
			// arrange
			String userId = "h2jinee";
			PointCriteria.Charge criteria1 = new PointCriteria.Charge(userId, 1000L);
			PointCriteria.Charge criteria2 = new PointCriteria.Charge(userId, 2000L);

			// act
			pointFacade.charge(criteria1);
			var result = pointFacade.charge(criteria2);

			// assert
			assertThat(result.balance()).isEqualTo(3000L);
			
			// DB 확인
			var point = pointJpaRepository.findByUserId(userId).orElse(null);
			assertThat(point).isNotNull();
			assertThat(point.getBalance().amount().longValue()).isEqualTo(3000L);
		}
		
		@DisplayName("포인트가 없는 유저가 충전하면 자동으로 생성된다.")
		@Test
		void createsPoint_whenUserHasNoPoints() {
			// arrange
			String newUserId = "new-user-" + System.nanoTime();
			// 새 유저 생성 (포인트 초기화 없이)
			UserCommand.Create userCommand = new UserCommand.Create(
				new UserId(newUserId),
				"새유저",
				User.Gender.M,
				new Birth("2000-01-01"),
				new Email("new" + System.nanoTime() + "@test.com")
			);
			userService.createUser(userCommand);
			
			PointCriteria.Charge criteria = new PointCriteria.Charge(newUserId, 5000L);

			// act
			var result = pointFacade.charge(criteria);

			// assert
			assertThat(result).isNotNull();
			assertThat(result.userId()).isEqualTo(newUserId);
			assertThat(result.balance()).isEqualTo(5000L);
		}
	}
}
