package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentCommand {
    
    /**
     * 포인트 결제 커맨드
     */
    public record Point(
        Long orderId,
        String userId,
        Money amount
    ) {
        public Point {
            if (orderId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (amount == null || amount.isNegativeOrZero()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "포인트 사용 금액은 0보다 커야 합니다.");
            }
        }
    }
    
    /**
     * PG 결제 커맨드
     */
    public record Pg(
        Long orderId,
        String userId,
        Money amount,
        PgPaymentInfo pgInfo
    ) {
        public Pg {
            if (orderId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (amount == null || amount.isNegativeOrZero()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다.");
            }
            if (pgInfo == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "PG 결제 정보는 필수입니다.");
            }
        }
    }
    
    /**
     * 내부 처리용 통합 커맨드 (Strategy 패턴용)
     */
    public record Process(
        Long orderId,
        String userId,
        Money amount,
        PaymentMethod paymentMethod,
        PgPaymentInfo pgInfo
    ) {
        public Process {
            if (orderId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (amount == null || amount.isNegative()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다.");
            }
            if (paymentMethod == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 수단은 필수입니다.");
            }
        }
        
        public static Process forPoint(Long orderId, String userId, Money amount) {
            return new Process(orderId, userId, amount, PaymentMethod.POINT, null);
        }
        
        public static Process forPg(Long orderId, String userId, Money amount, PgPaymentInfo pgInfo) {
            return new Process(orderId, userId, amount, PaymentMethod.PG, pgInfo);
        }
    }
}
