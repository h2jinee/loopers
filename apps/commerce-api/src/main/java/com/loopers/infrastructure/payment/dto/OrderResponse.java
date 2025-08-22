package com.loopers.infrastructure.payment.dto;

import java.util.List;

public record OrderResponse(
    String orderId,
    List<TransactionResponse> transactions
) {
    public record TransactionResponse(
        String transactionKey,
        String status,
        String reason
    ) {
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
        
        public boolean isPending() {
            return "PENDING".equals(status);
        }
        
        public boolean isFailed() {
            return "FAILED".equals(status);
        }
    }
}
