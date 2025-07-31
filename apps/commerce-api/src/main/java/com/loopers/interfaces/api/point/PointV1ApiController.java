package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.domain.point.vo.ChargePoint;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/point")
public class PointV1ApiController implements PointV1ApiSpec {

    private static final String USER_ID_HEADER = "X-USER-ID";

    private final PointService pointService;

    @PostMapping
    @Override
    public ApiResponse<PointDto.V1.Charge.Response> chargeUserPoint(
        @RequestBody @Valid PointDto.V1.Charge.Request request
    ) {
        PointCommand.Charge command = new PointCommand.Charge(
            request.userId(),
            new ChargePoint(request.amount())
        );

        PointEntity charged = pointService.charge(command);

        PointDto.V1.Charge.Response response = new PointDto.V1.Charge.Response(
            charged.getUserId(),
            charged.getPoint()
        );

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

        Long userPoint = pointService.getUserPoint(userId);

        PointDto.V1.GetPoint.Response response = new PointDto.V1.GetPoint.Response(
            userId,
            userPoint
        );

        return ApiResponse.success(response);
    }
}
