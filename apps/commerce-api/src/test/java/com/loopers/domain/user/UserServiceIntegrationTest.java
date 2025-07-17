package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

@SpringBootTest
public class UserServiceIntegrationTest {

	@MockitoSpyBean
    private UserRepository userRepository;

	@Autowired
	private UserService userService;

    @AfterEach
    void tearDown() {
        userRepository.clear();
    }

	/*
	* 회원 가입 통합 테스트
	- [x]  회원 가입 시 User 저장이 수행된다. ( spy 검증 )
	- [x]  이미 가입된 ID로 회원가입 시도 시, 실패한다.
	*/
	@DisplayName("회원 가입 시")
	@Nested
	class Join {
		@DisplayName("User 저장이 수행된다. ( spy 검증 )")
		@Test
		void savesUser_whenSignUpDataIsValid() {
			// arrange
			UserEntity user = new UserEntity(
				"h2jinee",
				"전희진",
				UserEntity.Gender.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);

			// act
			UserEntity savedUser = userService.save(user);

			// assert
			assertThat(savedUser).isNotNull();
			assertThat(savedUser.getUserId()).isEqualTo(user.getUserId());
			assertThat(savedUser.getName()).isEqualTo(user.getName());
			assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
			assertThat(savedUser.getBirth()).isEqualTo(user.getBirth());

			verify(userRepository, times(1)).save(any(UserEntity.class));
		}

		@DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
		@Test
		void fail_whenUserIdAlreadyExists() {
			// arrange
			UserEntity user = new UserEntity(
				"h2jinee",
				"전희진",
				UserEntity.Gender.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);

			UserEntity newUser = new UserEntity(
				"h2jinee",
				"김데빈",
				UserEntity.Gender.M,
				"2000-01-01",
				"devin@loopers.com"
			);

			// act
			userService.save(user);
			CoreException exception = assertThrows(CoreException.class, () -> {
				userService.save(newUser);
			});

			// assert
			assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
		}
	}

	/*
  	* 내 정보 조회 통합 테스트
	- [x]  해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.
	- [x]  해당 ID의 회원이 존재하지 않을 경우, null이 반환된다.
	*/
	@DisplayName("내 정보 조회 시")
	@Nested
	class GetUserInfo {
		@DisplayName("해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.")
		@Test
		void returnsUserInfo_whenUserExists() {
			// arrange
			UserEntity user = new UserEntity(
				"h2jinee",
				"전희진",
				UserEntity.Gender.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);
			userService.save(user);

			// act
			UserEntity foundUser = userService.findByUserId("h2jinee");

			// assert
			assertThat(foundUser).isNotNull();
			assertThat(foundUser).isEqualTo(user);
		}

		@DisplayName("해당 ID의 회원이 존재하지 않을 경우, null이 반환된다.")
		@Test
		void returnsNull_whenUserDoesNotExist() {
			// arrange
			UserEntity user = new UserEntity(
				"h2jinee",
				"전희진",
				UserEntity.Gender.F,
				"1997-01-18",
				"wjsgmlwls97@gmail.com"
			);
			userService.save(user);

			// act
			UserEntity foundUser = userService.findByUserId("devin");

			// assert
			assertThat(foundUser).isNull();
		}
	}
}
