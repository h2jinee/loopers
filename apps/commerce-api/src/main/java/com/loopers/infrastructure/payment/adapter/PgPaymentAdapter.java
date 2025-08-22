package com.loopers.infrastructure.payment.adapter;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.command.PgCancelCommand;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgCancelResult;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PaymentRequest;
import com.loopers.infrastructure.payment.dto.PaymentResponse;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentAdapter implements PgPaymentPort {

    private final PaymentGatewayClient paymentGatewayClient;
    private static final String CALLBACK_URL = "http://localhost:8080/api/v1/payments/callback";
    
    @Override
    public PgPaymentResult processPayment(PgPaymentCommand command) {
        try {
            PaymentRequest request = toPaymentRequest(command);
            
            ApiResponse<PaymentResponse> response = paymentGatewayClient.send(
                command.userId(),
                request
            );
            
            if (response == null || response.data() == null) {
                log.error("PG 응답이 null입니다. orderId={}", command.orderId());
                return PgPaymentResult.failure("PG 시스템 응답 없음");
            }
            
            PaymentResponse pgResponse = response.data();
            
            if (pgResponse.isSuccess()) {
                log.info("PG 결제 성공: transactionKey={}", pgResponse.transactionKey());
                return PgPaymentResult.success(pgResponse.transactionKey());
            } else {
                log.warn("PG 결제 실패: reason={}", pgResponse.reason());
                return PgPaymentResult.failure(pgResponse.reason());
            }
            
        } catch (Exception e) {
            log.error("PG 결제 처리 중 오류 발생: orderId={}", command.orderId(), e);
            return PgPaymentResult.failure("PG 시스템 오류: " + e.getMessage());
        }
    }

    /**
     * Domain Command를 Infrastructure DTO로 변환
     */
    private PaymentRequest toPaymentRequest(PgPaymentCommand command) {
        return new PaymentRequest(
            command.orderId().toString(),
            mapCardTypeToExternalCode(command.cardInfo().cardType()),
            command.cardInfo().cardNumber(),
            command.amount().amount().longValue(),
            CALLBACK_URL
        );
    }
    
    @Override
    public PgCancelResult cancelPayment(PgCancelCommand command) {
        try {
            // TODO: PG 취소 API 구현
            // PG Simulator에 취소 API가 없으므로 일단 임시 구현
            log.info("PG 결제 취소 요청: transactionId={}", command.transactionId());
            
            // 실제로는 paymentGatewayClient의 취소 API를 호출해야 함
            // ApiResponse<CancelResponse> response = paymentGatewayClient.cancel(
            //     command.userId(),
            //     command.transactionId()
            // );
            
            // 임시 구현
            return PgCancelResult.success("CANCEL_" + System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("PG 결제 취소 중 오류 발생: transactionId={}", command.transactionId(), e);
            return PgCancelResult.failure("PG 취소 실패: " + e.getMessage());
        }
    }
    
    /**
     * Domain CardType을 PG 시스템 코드로 변환
     * PG Simulator는 SAMSUNG, KB, HYUNDAI 문자열을 기대함
     */
    private String mapCardTypeToExternalCode(CardType cardType) {
        return switch (cardType) {
            case SAMSUNG -> "SAMSUNG";
            case KB -> "KB";
            case HYUNDAI -> "HYUNDAI";
        };
    }
}
