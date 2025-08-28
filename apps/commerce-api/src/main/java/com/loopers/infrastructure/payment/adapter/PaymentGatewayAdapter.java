package com.loopers.infrastructure.payment.adapter;

import com.loopers.domain.payment.port.PaymentGatewayPort;
import com.loopers.domain.payment.result.TransactionStatusResult;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PaymentGatewayPort의 Infrastructure 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayAdapter implements PaymentGatewayPort {
    
    private final PaymentGatewayClient paymentGatewayClient;
    
    @Override
    public TransactionStatusResult getTransactionStatus(String userId, String transactionKey) {
        try {
            var response = paymentGatewayClient.getTransaction(userId, transactionKey);
            
            if (response.meta().result() == ApiResponse.Metadata.Result.SUCCESS && response.data() != null) {
                var transaction = response.data();
                
                // PG 응답 상태를 도메인 상태로 변환
                return switch (transaction.status()) {
                    case "SUCCESS" -> TransactionStatusResult.success();
                    case "FAILED" -> TransactionStatusResult.failed(transaction.reason());
                    case "PENDING" -> TransactionStatusResult.pending();
                    default -> TransactionStatusResult.unknown();
                };
            }
            
            return TransactionStatusResult.unknown();
            
        } catch (Exception e) {
            log.error("PG 거래 상태 조회 실패: transactionKey={}", transactionKey, e);
            return TransactionStatusResult.error("PG 시스템 조회 오류: " + e.getMessage());
        }
    }
}
