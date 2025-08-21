package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;

public class UserResult {
    
    public record SignUpResult(
        String userId,
        String name,
        User.Gender gender,
        String birth,
        String email
    ) {
        public static SignUpResult from(UserInfo.SignUpResult domainInfo) {
            return new SignUpResult(
                domainInfo.userId(),
                domainInfo.name(),
                domainInfo.gender(),
                domainInfo.birth(),
                domainInfo.email()
            );
        }
    }
    
    public record Detail(
        String userId,
        String name,
        User.Gender gender,
        String birth,
        String email
    ) {
        public static Detail from(UserInfo.Detail domainInfo) {
            return new Detail(
                domainInfo.userId(),
                domainInfo.name(),
                domainInfo.gender(),
                domainInfo.birth(),
                domainInfo.email()
            );
        }
    }
}
