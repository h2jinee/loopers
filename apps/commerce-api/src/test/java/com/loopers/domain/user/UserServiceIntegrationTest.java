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

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;
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
			UserCommand.Create command = new UserCommand.Create(
				new UserId("h2jinee"),
				"전희진",
				UserEntity.Gender.F,
				new Birth("1997-01-18"),
				new Email("wjsgmlwls97@gmail.com")
			);

			// act
			UserEntity savedUser = userService.createUser(command);

			// assert
			assertThat(savedUser).isNotNull();
			assertThat(savedUser.getUserId()).isEqualTo("h2jinee");
			assertThat(savedUser.getName()).isEqualTo("전희진");
			assertThat(savedUser.getEmail()).isEqualTo("wjsgmlwls97@gmail.com");
			assertThat(savedUser.getBirth()).isEqualTo("1997-01-18");

			verify(userRepository, times(1)).save(any(UserEntity.class));
		}

		@DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
		@Test
		void fail_whenUserIdAlreadyExists() {
			// arrange
			UserCommand.Create user = new UserCommand.Create(
				new UserId("h2jinee"),
				"전희진",
				UserEntity.Gender.F,
				new Birth("1997-01-18"),
				new Email("wjsgmlwls97@gmail.com")
			);

			UserCommand.Create newUser = new UserCommand.Create(
				new UserId("h2jinee"),
				"김데빈",
				UserEntity.Gender.M,
				new Birth("2000-01-01"),
				new Email("devin@loopers.com")
			);

			// act
			userService.createUser(user);
			CoreException exception = assertThrows(CoreException.class, () -> {
				userService.createUser(newUser);
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
		void returnsUserInfo_whenUserIdExists() {
			// arrange
			UserCommand.Create command = new UserCommand.Create(
				new UserId("h2jinee"),
				"전희진",
				UserEntity.Gender.F,
				new Birth("1997-01-18"),
				new Email("wjsgmlwls97@gmail.com")
			);
			UserEntity user = userService.createUser(command);

			// act
			UserEntity foundUser = userService.getUserInfo("h2jinee");

			// assert
			assertThat(foundUser).isNotNull();
			assertThat(foundUser.getUserId()).isEqualTo(user.getUserId());
			assertThat(foundUser.getName()).isEqualTo(user.getName());
			assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
		}

		@DisplayName("해당 ID의 회원이 존재하지 않을 경우, null이 반환된다.")
		@Test
		void returnsNull_whenUserIdDoesNotExist() {
			// arrange
			UserCommand.Create command = new UserCommand.Create(
				new UserId("h2jinee"),
				"전희진",
				UserEntity.Gender.F,
				new Birth("1997-01-18"),
				new Email("wjsgmlwls97@gmail.com")
			);
			userService.createUser(command);

			// act & assert
			CoreException exception = assertThrows(CoreException.class, () -> {
				userService.getUserInfo("devin");
			});

			// assert
			assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
			assertThat(exception.getMessage()).isEqualTo("존재하지 않는 사용자입니다.");
		}
	}
}
