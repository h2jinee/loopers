package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResultCommand;
import com.loopers.domain.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentResultDto {
    
    public record ResultRequest(
        @NotBlank(message = "거래키는 필수입니다")
        String transactionKey,
        
        @NotBlank(message = "주문ID는 필수입니다")
        String orderId,
        
        @NotNull(message = "결제 상태는 필수입니다")
        PaymentStatus status,
        
        String reason  // 실패 사유 (실패시에만)
    ) {
        public PaymentResultCommand toCommand() {
            return new PaymentResultCommand(
                transactionKey,
                Long.parseLong(orderId),
                status == PaymentStatus.COMPLETED,
                reason
            );
        }
    }

    public record ResultResponse(
        String message,
        boolean accepted
    ) {
        public static ResultResponse createAccepted() {
            return new ResultResponse("결제 결과가 정상적으로 처리되었습니다", true);
        }
        
        public static ResultResponse createRejected(String reason) {
            return new ResultResponse("결제 결과 처리가 거부되었습니다: " + reason, false);
        }
    }

}
