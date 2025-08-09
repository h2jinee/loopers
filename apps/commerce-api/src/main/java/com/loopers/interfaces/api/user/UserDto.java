package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserEntity;

import jakarta.validation.constraints.Email;
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
                UserEntity.Gender gender,

                @NotNull
                @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일은 yyyy-MM-dd 형식")
                String birth,

                @NotNull
                @Email(message = "올바른 이메일 형식이어야 합니다")
                String email
            ) {

            }

            public record Response(
                String userId,
                String name,
				UserEntity.Gender gender,
				String birth,
                String email
            ) {
                public static Response from(com.loopers.application.user.UserResult.SignUpResult result) {
                    return new Response(
                        result.userId(),
                        result.name(),
                        result.gender(),
                        result.birth(),
                        result.email()
                    );
                }
            }
        }

        // 사용자 조회
        public static class GetUser {
            public record Response(
                String userId,
                String name,
                UserEntity.Gender gender,
                String birth,
                String email
            ) {
                public static Response from(com.loopers.application.user.UserResult.Detail result) {
                    return new Response(
                        result.userId(),
                        result.name(),
                        result.gender(),
                        result.birth(),
                        result.email()
                    );
                }
            }
        }
    }
}
