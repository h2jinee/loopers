package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.point.PointCriteria;
import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1ApiController implements PointV1ApiSpec {

    private static final String USER_ID_HEADER = "X-USER-ID";

    private final PointFacade pointFacade;

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointDto.V1.Charge.Response> chargeUserPoint(
        @RequestBody @Valid PointDto.V1.Charge.Request request
    ) {
        PointCriteria.Charge criteria = new PointCriteria.Charge(request.userId(), request.amount());
        PointResult.ChargeResult result = pointFacade.chargeUserPoint(criteria);

        PointDto.V1.Charge.Response response = PointDto.V1.Charge.Response.from(result);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Override
    public ApiResponse<PointDto.V1.GetPoint.Response> getUserPoint(
        @RequestHeader(value = USER_ID_HEADER, required = false) String userId
    ) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, USER_ID_HEADER + " 헤더가 없습니다.");
        }

        PointCriteria.GetDetail criteria = new PointCriteria.GetDetail(userId);
        PointResult.Detail result = pointFacade.getUserPoint(criteria);

        PointDto.V1.GetPoint.Response response = PointDto.V1.GetPoint.Response.from(result);
        return ApiResponse.success(response);
    }
}
