package com.loopers.application.user;

import org.springframework.stereotype.Service;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final PointService pointService;

	@Transactional
    public UserResult.SignUpResult signUp(UserCriteria.SignUp criteria) {
        // 1. 사용자 생성 및 중복 검증
        UserCommand.Create command = criteria.toCommand();
        UserEntity savedUser = userService.createUser(command);
        
        // 2. 새 사용자 포인트 초기화 (0포인트)
        PointCommand.Initialize initializeCommand = new PointCommand.Initialize(savedUser.getUserId());
        pointService.initializeUserPoint(initializeCommand);
        
        // 3. 회원가입 결과 응답 생성
        UserInfo.SignUpResult domainInfo = UserInfo.SignUpResult.from(savedUser);
        return UserResult.SignUpResult.from(domainInfo);
    }
    
    public UserResult.Detail getUserInfo(UserCriteria.GetDetail criteria) {
        // 1. 사용자 정보 조회
        UserCommand.GetOne command = criteria.toCommand();
        UserEntity user = userService.getUserInfo(command);
        
        // 2. 사용자 상세 정보 응답 생성
        UserInfo.Detail domainInfo = UserInfo.Detail.from(user);
        return UserResult.Detail.from(domainInfo);
    }
}
