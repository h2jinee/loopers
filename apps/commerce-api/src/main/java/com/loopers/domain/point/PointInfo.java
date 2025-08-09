package com.loopers.domain.point;

public class PointInfo {
    
    public record ChargeResult(
        String userId,
        Long balance
    ) {
        public static ChargeResult from(PointEntity point) {
            return new ChargeResult(
                point.getUserId(),
                point.getBalance().amount().longValue()
            );
        }
    }
    
    public record Detail(
        String userId,
        Long balance
    ) {
        public static Detail from(PointEntity point) {
            return new Detail(
                point.getUserId(),
                point.getBalance().amount().longValue()
            );
        }
    }
}