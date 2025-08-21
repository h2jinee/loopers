package com.loopers.domain.user;

public class UserInfo {
    
    public record SignUpResult(
        String userId,
        String name,
        User.Gender gender,
        String birth,
        String email
    ) {
        public static SignUpResult from(User user) {
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
        User.Gender gender,
        String birth,
        String email
    ) {
        public static Detail from(User user) {
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
