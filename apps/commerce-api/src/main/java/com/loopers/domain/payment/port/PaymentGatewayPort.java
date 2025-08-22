package com.loopers.domain.payment.port;

import com.loopers.domain.payment.result.TransactionStatusResult;

/**
 * PG 시스템과의 통신을 위한 Port 인터페이스
 */
public interface PaymentGatewayPort {
    TransactionStatusResult getTransactionStatus(String userId, String transactionKey);
}
