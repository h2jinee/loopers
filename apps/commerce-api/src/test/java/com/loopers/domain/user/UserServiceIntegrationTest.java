package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
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
	* 통합 테스트
	  - [x]  회원 가입시 User 저장이 수행된다. ( spy 검증 )
	  - [x]  이미 가입된 ID 로 회원가입 시도 시, 실패한다.
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
				"wjsgmlwls97@gmail.com",
				"1997-01-18"
			);

			// act
			UserEntity savedUser = userService.save(user);

			// assert
			assertAll (
				() -> {
					assertNotNull(savedUser);
					assertThat(savedUser.getUserId()).isEqualTo(user.getUserId());
				},
				() -> {
					assertNotNull(savedUser);
					assertThat(savedUser.getName()).isEqualTo(user.getName());
				},
				() -> {
					assertNotNull(savedUser);
					assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
				},
				() -> {
					assertNotNull(savedUser);
					assertThat(savedUser.getBirth()).isEqualTo(user.getBirth());
				}
			);

			verify(userRepository, times(1)).save(any(UserEntity.class));
		}

		@DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
		@Test
		void fail_whenUserIdAlreadyExists() {
			// arrange
			UserEntity user = new UserEntity(
				"h2jinee",
				"전희진",
				"wjsgmlwls97@gmail.com",
				"1997-01-18"
			);

			UserEntity newUser = new UserEntity(
				"h2jinee",
				"김데빈",
				"devin@loopers.com",
				"2000-01-01"
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

}
