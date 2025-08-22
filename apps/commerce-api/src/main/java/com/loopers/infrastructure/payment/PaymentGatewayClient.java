package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.OrderResponse;
import com.loopers.infrastructure.payment.dto.PaymentRequest;
import com.loopers.infrastructure.payment.dto.PaymentResponse;
import com.loopers.infrastructure.payment.dto.TransactionDetailResponse;
import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "payment-gateway", 
    url = "${pg.simulator.url:http://localhost:8082}"
)
public interface PaymentGatewayClient {

    /**
     * 결제 요청
     * POST /api/v1/payments
     */
    @PostMapping("/api/v1/payments")
    ApiResponse<PaymentResponse> send(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody PaymentRequest paymentRequest
    );

    /**
     * 특정 거래 조회 (transactionKey로 조회)
     * GET /api/v1/payments/{transactionKey}
     */
    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<TransactionDetailResponse> getTransaction(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable("transactionKey") String transactionKey
    );

    /**
     * 주문별 거래 조회
     * GET /api/v1/payments?orderId={orderId}
     */
    @GetMapping("/api/v1/payments")
    ApiResponse<OrderResponse> getTransactionsByOrder(
        @RequestHeader("X-USER-ID") String userId,
        @RequestParam(name = "orderId", required = false) String orderId
    );
}
