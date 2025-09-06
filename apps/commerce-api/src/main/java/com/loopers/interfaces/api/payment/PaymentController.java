package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    /**
     * 결제 시작
     */
    @PostMapping
    @Override
    public ApiResponse<PaymentDto.V1.Initiate.Response> initiatePayment(
        @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody PaymentDto.V1.Initiate.Request request
    ) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }

        log.info("결제 시작 - userId: {}, orderId: {}, method: {}",
            userId, request.orderId(), request.paymentMethod());

        PaymentResult result = paymentFacade.initiatePayment(userId, request);
        return ApiResponse.success(PaymentDto.V1.Initiate.Response.from(request.orderId(), result));
    }

    /**
     * PG 결제 콜백
     */
    @PostMapping("/callback")
    @Override
    public ApiResponse<PaymentDto.V1.Callback.Response> handlePaymentCallback(
        @Valid @RequestBody PaymentDto.V1.Callback.Request request
    ) {
        log.info("PG 콜백 수신 - transactionKey: {}, status: {}",
            request.transactionKey(), request.status());

        paymentFacade.processPaymentResult(request.toCommand());
        return ApiResponse.success(PaymentDto.V1.Callback.Response.createAccepted());
    }
}
