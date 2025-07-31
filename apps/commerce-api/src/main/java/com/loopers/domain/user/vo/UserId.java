package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record UserId(String value) {
    private static final String PATTERN = "^[a-zA-Z0-9]{1,10}$";

    public UserId {
        if (value == null || !value.matches(PATTERN)) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "ID는 영문 및 숫자 10자 이내로 입력해야 합니다."
            );
        }
    }
}
