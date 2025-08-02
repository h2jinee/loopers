package com.loopers.application.user;

import org.springframework.stereotype.Service;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointDomainService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserDomainService;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;
import com.loopers.interfaces.api.user.UserDto;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserDomainService userDomainService;
    private final PointDomainService pointDomainService;

	@Transactional
    public UserDto.V1.SignUp.Response signUp(UserDto.V1.SignUp.Request request) {
        UserCommand.Create command = new UserCommand.Create(
            new UserId(request.userId()),
            request.name(),
            request.gender(),
            new Birth(request.birth()),
            new Email(request.email())
        );
        
        UserEntity savedUser = userDomainService.createUser(command);
        
        PointCommand.Initialize initializeCommand = new PointCommand.Initialize(savedUser.getUserId());
        pointDomainService.initializeUserPoint(initializeCommand);
        
        return new UserDto.V1.SignUp.Response(
            savedUser.getUserId(),
            savedUser.getName(),
            savedUser.getGender(),
			savedUser.getBirth(),
			savedUser.getEmail()
        );
    }
    
    public UserDto.V1.GetUser.Response getUserInfo(String userId) {
        UserCommand.GetOne command = new UserCommand.GetOne(userId);
        UserEntity user = userDomainService.getUserInfo(command);
        
        return new UserDto.V1.GetUser.Response(
            user.getUserId(),
            user.getName(),
            user.getGender(),
            user.getBirth(),
            user.getEmail()
        );
    }
}
