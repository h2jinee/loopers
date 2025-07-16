package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class UserEntity {
	@Id
	private String userId;
	private String name;
	private String email;
	private String birth;

	private static final String PATTERN_USER_ID = "^[a-zA-Z0-9]{1,10}$";
	private static final String PATTERN_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	private static final String PATTERN_BIRTH = "^\\d{4}-\\d{2}-\\d{2}$";

	UserEntity(
		String userId,
		String name,
		String email,
		String birth
	) {
		if (userId == null || !userId.matches(PATTERN_USER_ID)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"ID는 영문 및 숫자 10자 이내로 입력해야 합니다."
			);
		}
		if (email == null || !email.matches(PATTERN_EMAIL)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"이메일은 xx@yy.zz 형식이어야 합니다."
			);
		}
		if (birth == null || !birth.matches(PATTERN_BIRTH)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"생년월일은 yyyy-MM-dd 형식이어야 합니다."
			);
		}
	}
}
