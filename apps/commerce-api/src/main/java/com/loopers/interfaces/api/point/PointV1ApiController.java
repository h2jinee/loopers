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
import com.loopers.domain.point.PointDomainService;
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

    private final PointDomainService pointDomainService;

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointDto.V1.Charge.Response> chargeUserPoint(
        @RequestBody @Valid PointDto.V1.Charge.Request request
    ) {
        PointCommand.Charge command = new PointCommand.Charge(
            request.userId(),
            new ChargePoint(request.amount())
        );

        PointEntity charged = pointDomainService.charge(command);

        PointDto.V1.Charge.Response response = new PointDto.V1.Charge.Response(
            charged.getUserId(),
            charged.getBalance().amount().longValue()
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

        PointCommand.GetOne command = new PointCommand.GetOne(userId);
        PointEntity point = pointDomainService.getPointEntity(command);

        PointDto.V1.GetPoint.Response response = new PointDto.V1.GetPoint.Response(
            userId,
            point.getBalance().amount().longValue()
        );

        return ApiResponse.success(response);
    }
}
