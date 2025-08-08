package com.loopers.domain.point;

import com.loopers.domain.common.Money;
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
    
    public record Use(
        String userId,
        Money amount,
        Long orderId
    ) {
        public Use {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (amount == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용 금액은 필수입니다.");
            }
            if (orderId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
            }
        }
    }

    
    public record GetOne(
        String userId
    ) {
        public GetOne {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
    
    public record Initialize(
        String userId
    ) {
        public Initialize {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
}
