package com.loopers.application.point;

import com.loopers.domain.point.PointInfo;

public class PointResult {
    
    public record ChargeResult(
        String userId,
        Long balance
    ) {
        public static ChargeResult from(PointInfo.ChargeResult domainInfo) {
            return new ChargeResult(
                domainInfo.userId(),
                domainInfo.balance()
            );
        }
    }
    
    public record Detail(
        String userId,
        Long balance
    ) {
        public static Detail from(PointInfo.Detail domainInfo) {
            return new Detail(
                domainInfo.userId(),
                domainInfo.balance()
            );
        }
    }
}