package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User V1 API", description = "사용자 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(summary = "회원가입")
    ApiResponse<UserDto.V1.SignUp.Response> signUp(
        @Valid @RequestBody UserDto.V1.SignUp.Request signUpRequest
    );

    @Operation(summary = "사용자 정보 조회")
    ApiResponse<UserDto.V1.GetUser.Response> getUserInfo(
        @PathVariable String userId
    );
}
