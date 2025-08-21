package com.loopers.domain.point;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class PointInfo {
    
    /**
     * 포인트 충전 결과
     */
    public record ChargeResult(
        String userId,
        BigDecimal previousBalance,
        BigDecimal chargedAmount,
        BigDecimal newBalance,
        ZonedDateTime chargedAt
    ) {
        public static ChargeResult from(Point point) {
            // 충전 후 잔액에서 충전 금액을 빼서 이전 잔액 계산
            return new ChargeResult(
                point.getUserId(),
                point.getBalance().amount(),
                BigDecimal.ZERO, // 실제 충전 금액은 히스토리에서
                point.getBalance().amount(),
                point.getCreatedAt()
            );
        }
        
        public static ChargeResult from(Point point, PointHistory history) {
            BigDecimal newBalance = point.getBalance().amount();
            BigDecimal chargedAmount = history.getAmount().amount();
            BigDecimal previousBalance = newBalance.subtract(chargedAmount);
            
            return new ChargeResult(
                point.getUserId(),
                previousBalance,
                chargedAmount,
                newBalance,
                history.getCreatedAt()
            );
        }
    }
    
    /**
     * 포인트 상세 정보
     */
    public record Detail(
        String userId,
        BigDecimal balance,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        public static Detail from(Point point) {
            return new Detail(
                point.getUserId(),
                point.getBalance().amount(),
                point.getCreatedAt(),
                point.getUpdatedAt()
            );
        }
    }
    
    /**
     * 포인트 사용 결과
     */
    public record UseResult(
        String userId,
        BigDecimal usedAmount,
        BigDecimal remainingBalance,
        Long orderId,
        ZonedDateTime usedAt
    ) {
        public static UseResult from(Point point, PointHistory history) {
            return new UseResult(
                point.getUserId(),
                history.getAmount().amount(),
                point.getBalance().amount(),
                history.getOrderId(),
                history.getCreatedAt()
            );
        }
    }
    
    /**
     * 포인트 간단 정보 (하위 호환성 유지)
     */
    public record Charged(
        String userId,
        Long balance
    ) {
        public static Charged from(Point point) {
            return new Charged(
                point.getUserId(),
                point.getBalance().amount().longValue()
            );
        }
    }
}
