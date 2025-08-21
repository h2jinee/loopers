package com.loopers.application.point;

import com.loopers.domain.point.PointInfo;

public class PointResult {
    
    public record Charged(
        String userId,
        Long balance
    ) {
        public static Charged from(PointInfo.Charged domainInfo) {
            return new Charged(
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
                domainInfo.balance().longValue()
            );
        }
    }
}
