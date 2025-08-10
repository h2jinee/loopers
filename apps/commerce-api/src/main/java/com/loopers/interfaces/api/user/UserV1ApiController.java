package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.user.UserCriteria;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserResult;
import com.loopers.interfaces.api.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1ApiController implements UserV1ApiSpec {
    
    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserDto.V1.SignUp.Response> signUp(
        @Valid @RequestBody UserDto.V1.SignUp.Request request
    ) {
        UserCriteria.SignUp criteria = new UserCriteria.SignUp(
            request.userId(), 
            request.name(), 
            request.gender(), 
            request.birth(), 
            request.email()
        );
        UserResult.SignUpResult result = userFacade.signUp(criteria);
        UserDto.V1.SignUp.Response response = UserDto.V1.SignUp.Response.from(result);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/me")
    @Override
    public ApiResponse<UserDto.V1.GetUser.Response> getUserInfo(
        @RequestHeader(value = "X-USER-ID") String userId
    ) {
        UserCriteria.GetDetail criteria = new UserCriteria.GetDetail(userId);
        UserResult.Detail result = userFacade.getUserInfo(criteria);
        UserDto.V1.GetUser.Response response = UserDto.V1.GetUser.Response.from(result);
        return ApiResponse.success(response);
    }
}
