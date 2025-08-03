package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class LikeEntityTest {

    /*
     * Like 생성 단위 테스트
     * - [x] userId가 null이면 BAD_REQUEST를 반환한다.
     * - [x] userId가 빈 문자열이면 BAD_REQUEST를 반환한다.
     * - [x] productId가 null이면 BAD_REQUEST를 반환한다.
     */
    @DisplayName("Like 생성 시")
    @Nested
    class Create {
        @DisplayName("userId가 null이면 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenUserIdIsNull() {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                LikeEntity like = new LikeEntity(null, 1L);
                like.guard();
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("사용자 ID는 필수입니다.");
        }

        @DisplayName("userId가 빈 문자열이면 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenUserIdIsBlank() {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                LikeEntity like = new LikeEntity("", 1L);
                like.guard();
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("사용자 ID는 필수입니다.");
        }

        @DisplayName("productId가 null이면 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenProductIdIsNull() {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                LikeEntity like = new LikeEntity("testUser", null);
                like.guard();
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("상품 ID는 필수입니다.");
        }
    }
}
