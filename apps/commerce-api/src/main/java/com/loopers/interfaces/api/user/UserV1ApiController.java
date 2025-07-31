package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.user.UserApplicationService;
import com.loopers.interfaces.api.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1ApiController implements UserV1ApiSpec {
    
    private final UserApplicationService userApplicationService;

    @PostMapping
    @Override
    public ApiResponse<UserDto.V1.SignUp.Response> signUp(
        @Valid @RequestBody UserDto.V1.SignUp.Request request
    ) {
        UserDto.V1.SignUp.Response response = userApplicationService.signUp(request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("{userId}")
    @Override
    public ApiResponse<UserDto.V1.GetUser.Response> getUserInfo(@PathVariable String userId) {
        UserDto.V1.GetUser.Response response = userApplicationService.getUserInfo(userId);
        return ApiResponse.success(response);
    }
}
