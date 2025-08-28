package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.vo.UserId;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UserDto {

    public static class V1 {

        // 회원가입
        public static class SignUp {

            public record Request(
                @NotNull
                @Pattern(regexp = "^[a-zA-Z0-9]{1,10}$", message = "ID는 영문 및 숫자 10자 이내")
                String userId,

                @NotNull
                String name,

                @NotNull
                User.Gender gender,

                @NotNull
                @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일은 yyyy-MM-dd 형식")
                String birth,

                @NotNull
                @jakarta.validation.constraints.Email(message = "올바른 이메일 형식이어야 합니다")
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

            public record Response(
                String userId,
                String name
            ) {
                public static Response from(User user) {
                    return new Response(
                        user.getUserId(),
                        user.getName()
                    );
                }
            }
        }

        // 사용자 조회
        public static class GetUser {
            public record Response(
                String userId,
                String name,
                String email,
                String birth,
                String gender
            ) {
                public static Response from(User user) {
                    return new Response(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getBirth(),
                        user.getGender().name()
                    );
                }
            }
        }
    }
}
