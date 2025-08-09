package com.loopers.domain.user;

public class UserInfo {
    
    public record SignUpResult(
        String userId,
        String name,
        UserEntity.Gender gender,
        String birth,
        String email
    ) {
        public static SignUpResult from(UserEntity user) {
            return new SignUpResult(
                user.getUserId(),
                user.getName(),
                user.getGender(),
                user.getBirth(),
                user.getEmail()
            );
        }
    }
    
    public record Detail(
        String userId,
        String name,
        UserEntity.Gender gender,
        String birth,
        String email
    ) {
        public static Detail from(UserEntity user) {
            return new Detail(
                user.getUserId(),
                user.getName(),
                user.getGender(),
                user.getBirth(),
                user.getEmail()
            );
        }
    }
}