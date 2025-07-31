package com.loopers.domain.point.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record ChargePoint(Long value) {
    public ChargePoint {
        if (value == null || value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }
    }
}
