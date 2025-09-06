package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResultCommand;
import com.loopers.domain.common.Money;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentDto {
    public static class V1 {

        /**
         * 결제 시작
         */
        public static class Initiate {
            public record Request(
                @NotNull(message = "주문 ID는 필수입니다.")
                Long orderId,

                @NotNull(message = "결제 금액은 필수입니다.")
                @Positive(message = "결제 금액은 양수여야 합니다.")
                BigDecimal amount,

                @NotNull(message = "결제 방법은 필수입니다.")
                PaymentMethod paymentMethod,

                // PG 결제 시 필요
                String cardType,
                String cardNo
            ) {
                public Money toMoney() {
                    return Money.of(amount);
                }
            }

            public record Response(
                Long orderId,
                String transactionKey,
                BigDecimal amount,
                PaymentMethod paymentMethod,
                PaymentStatus status
            ) {
                public static Response from(Long orderId, PaymentResult result) {
                    return new Response(
                        orderId,
                        result.transactionKey(),
                        result.amount().amount(),
                        result.method(),
                        result.isSuccess() ? PaymentStatus.PENDING : PaymentStatus.FAILED
                    );
                }
            }
        }

        /**
         * PG 결제 콜백
         */
        public static class Callback {
            public record Request(
                @NotBlank(message = "거래키는 필수입니다")
                String transactionKey,

                @NotBlank(message = "주문ID는 필수입니다")
                String orderId,

                @NotNull(message = "결제 상태는 필수입니다")
                String status,  // String으로 변경하여 외부 시스템과 호환

                String reason  // 실패 사유
            ) {
                public PaymentResultCommand toCommand() {
                    return PaymentResultCommand.basicResult(
                        transactionKey,
                        Long.parseLong(orderId),
                        mapToSuccessStatus(status),
                        reason
                    );
                }
                
                /**
                 * 외부 PG사의 다양한 status 값을 내부 도메인 모델로 매핑
                 */
                private boolean mapToSuccessStatus(String externalStatus) {
                    return switch (externalStatus.toUpperCase()) {
                        case "SUCCESS", "COMPLETED", "PAID", "DONE" -> true;  // 성공
                        case "FAILED", "ERROR", "REJECTED", "CANCELLED" -> false;  // 실패
                        case "PENDING", "PROCESSING" -> false;  // 대기중도 일단 실패로 처리
                        default -> false;  // 알 수 없는 상태는 실패로 처리
                    };
                }
            }

            public record Response(
                String message,
                boolean accepted
            ) {
                public static Response createAccepted() {
                    return new Response("결제 결과가 정상적으로 처리되었습니다", true);
                }

                public static Response createRejected(String reason) {
                    return new Response("결제 결과 처리가 거부되었습니다: " + reason, false);
                }
            }
        }
    }
}
