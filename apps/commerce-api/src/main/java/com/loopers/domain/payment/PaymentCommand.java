package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.point.Point;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentCommand {
    
    public record ProcessPayment(
        Order order,
        String userId
    ) {
        public ProcessPayment {
            if (order == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 정보는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
    
    public record ProcessPaymentWithPoint(
        Order order,
        String userId,
        Point point
    ) {
        public ProcessPaymentWithPoint {
            if (order == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 정보는 필수입니다.");
            }
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (point == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "포인트 정보는 필수입니다.");
            }
        }
        
        public static ProcessPaymentWithPoint from(ProcessPayment command, Point point) {
            return new ProcessPaymentWithPoint(command.order(), command.userId(), point);
        }
    }
}
