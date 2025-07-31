package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Birth(String value) {
    private static final String PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

    public Birth {
        if (value == null || !value.matches(PATTERN)) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "생년월일은 yyyy-MM-dd 형식이어야 합니다."
            );
        }
    }
}
