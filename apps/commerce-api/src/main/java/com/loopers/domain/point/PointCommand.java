package com.loopers.domain.point;

import com.loopers.domain.point.vo.ChargePoint;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PointCommand {
    public record Charge(
        String userId,
        ChargePoint amount
    ) {
        public Charge {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
}
