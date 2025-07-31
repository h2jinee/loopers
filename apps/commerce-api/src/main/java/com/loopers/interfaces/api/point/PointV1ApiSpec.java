package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Point V1 API", description = "포인트 API 입니다.")
public interface PointV1ApiSpec {

    @Operation(summary = "포인트 충전")
    ApiResponse<PointDto.V1.Charge.Response> chargeUserPoint(
        @Valid @RequestBody PointDto.V1.Charge.Request request
    );

    @Operation(summary = "포인트 조회")
    ApiResponse<PointDto.V1.GetPoint.Response> getUserPoint(
        @Parameter(hidden = true) @RequestHeader("X-USER-ID") String userId
    );
}
