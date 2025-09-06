package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Payment V1 API", description = "결제 관련 API")
public interface PaymentV1ApiSpec {

    @Operation(summary = "결제 시작")
    ApiResponse<PaymentDto.V1.Initiate.Response> initiatePayment(
        @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId,
        @Valid @RequestBody PaymentDto.V1.Initiate.Request request
    );

    @Operation(summary = "PG 결제 콜백 처리")
    ApiResponse<PaymentDto.V1.Callback.Response> handlePaymentCallback(
        @Valid @RequestBody PaymentDto.V1.Callback.Request request
    );
}
