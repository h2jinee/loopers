package com.loopers.domain.user;

import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class UserValidator {
	private static final String PATTERN_USER_ID = "^[a-zA-Z0-9]{1,10}$";
	private static final String PATTERN_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	private static final String PATTERN_BIRTH = "^\\d{4}-\\d{2}-\\d{2}$";

	public static void validateUserId(String userId) {
		if (userId == null || !userId.matches(PATTERN_USER_ID)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"ID는 영문 및 숫자 10자 이내로 입력해야 합니다."
			);
		}
	}

	public static void validateEmail(String email) {
		if (email == null || !email.matches(PATTERN_EMAIL)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"이메일은 xx@yy.zz 형식이어야 합니다."
			);
		}
	}

	public static void validateBirth(String birth) {
		if (birth == null || !birth.matches(PATTERN_BIRTH)) {
			throw new CoreException(
				ErrorType.BAD_REQUEST,
				"생년월일은 yyyy-MM-dd 형식이어야 합니다."
			);
		}
	}

	public static void validateGender(UserV1Dto.SignUpRequest.GenderRequest gender) {
        if (gender == null) {
            throw new CoreException(
				ErrorType.BAD_REQUEST,
				"성별은 필수 값입니다."
			);
        }
    }


}
