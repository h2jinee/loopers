package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Email(String value) {
    private static final String PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public Email {
        if (value == null || !value.matches(PATTERN)) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "이메일은 xx@yy.zz 형식이어야 합니다."
            );
        }
    }
}
