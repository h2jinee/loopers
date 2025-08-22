package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentFacade paymentFacade;
    
    /**
     * PG 결제 결과 처리
     * - PG에서 비동기로 결제 결과 전송
     * - 즉시 응답 후 비동기 처리
     */
    @PostMapping("/callback")
    public ApiResponse<PaymentResultDto.ResultResponse> handlePaymentResult(
        @Valid @RequestBody PaymentResultDto.ResultRequest request
    ) {
        log.info("PG 결제 결과 수신: transactionKey={}, status={}", 
            request.transactionKey(), request.status());
        
        try {
            paymentFacade.processPaymentResult(request.toCommand());
            
            return ApiResponse.success(PaymentResultDto.ResultResponse.createAccepted());
        } catch (Exception e) {
            log.error("결제 결과 처리 실패: ", e);
            return ApiResponse.success(PaymentResultDto.ResultResponse.createRejected(e.getMessage()));
        }
    }
}
