package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;

public class UserCriteria {
    
    public record SignUp(
        String userId,
        String name,
        UserEntity.Gender gender,
        String birth,
        String email
    ) {
        public UserCommand.Create toCommand() {
            return new UserCommand.Create(
                new UserId(userId),
                name,
                gender,
                new Birth(birth),
                new Email(email)
            );
        }
    }
    
    public record GetDetail(
        String userId
    ) {
        public UserCommand.GetOne toCommand() {
            return new UserCommand.GetOne(userId);
        }
    }
}