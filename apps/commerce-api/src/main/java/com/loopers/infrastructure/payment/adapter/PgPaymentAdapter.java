package com.loopers.infrastructure.payment.adapter;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PaymentRequest;
import com.loopers.infrastructure.payment.dto.PaymentResponse;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentAdapter implements PgPaymentPort {

    private final PaymentGatewayClient paymentGatewayClient;

    @Value("${payment.callback.url}")
    private String callbackUrl;

    @Override
    public PgPaymentResult processPayment(PgPaymentCommand command) {
        String tempTransactionId = generateTempTransactionId(command.orderId());
        PaymentRequest request = toPaymentRequest(command);
        ApiResponse<PaymentResponse> response = paymentGatewayClient.send(command.userId(), request);

        if (response == null || response.data() == null) {
            log.error("PG 응답이 null입니다. orderId={}", command.orderId());
            return PgPaymentResult.pending(tempTransactionId, "PG 시스템 응답 없음");
        }

        PaymentResponse pgResponse = response.data();

        if (pgResponse.isSuccess()) {
            log.info("PG 결제 요청 성공: transactionKey={}", pgResponse.transactionKey());
            return PgPaymentResult.pending(pgResponse.transactionKey(), "결제 처리중");
        } else {
            log.warn("PG 결제 실패: reason={}", pgResponse.reason());
            return PgPaymentResult.failure(pgResponse.reason());
        }
    }

    private PaymentRequest toPaymentRequest(PgPaymentCommand command) {
        return new PaymentRequest(command.orderId().toString(), mapCardTypeToExternalCode(command.cardInfo().cardType()),
            command.cardInfo().cardNo(), command.amount().amount().longValue(), callbackUrl);
    }

    private String mapCardTypeToExternalCode(CardType cardType) {
        return switch (cardType) {
            case SAMSUNG -> "SAMSUNG";
            case KB -> "KB";
            case HYUNDAI -> "HYUNDAI";
        };
    }

    private String generateTempTransactionId(Long orderId) {
        return String.format("TEMP_%s_%s", orderId, UUID.randomUUID().toString().substring(0, 8));
    }
}
