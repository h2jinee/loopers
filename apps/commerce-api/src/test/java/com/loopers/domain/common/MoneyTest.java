package com.loopers.domain.common;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.math.BigDecimal;

public class MoneyTest {

    /*
     * Money 생성 단위 테스트
     * - [x] 음수 금액으로 Money 생성 시 BAD_REQUEST를 반환한다.
     * - [x] null 금액으로 Money 생성 시 BAD_REQUEST를 반환한다.
     */
    @DisplayName("Money 생성 시")
    @Nested
    class Create {

        @DisplayName("음수 금액으로 Money 생성 시 BAD_REQUEST를 반환한다.")
        @ParameterizedTest
        @ValueSource(longs = {-1, -100, -999999999})
        void fail_whenAmountIsNegative(long amount) {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                Money.of(amount);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("금액은 0원 이상이어야 합니다.");
        }

        @DisplayName("null 금액으로 Money 생성 시 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenAmountIsNull() {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                Money.of((BigDecimal) null);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("금액은 null일 수 없습니다.");
        }
    }

    /*
     * Money 연산 단위 테스트
     * - [x] 두 Money를 더할 수 있다.
     * - [x] null Money와 더하기 시 BAD_REQUEST를 반환한다.
     * - [x] 두 Money를 뺄 수 있다.
     * - [x] 더 큰 금액을 빼면 BAD_REQUEST를 반환한다.
     * - [x] null Money와 빼기 시 BAD_REQUEST를 반환한다.
     * - [x] Money를 양수로 곱할 수 있다.
     * - [x] Money를 0으로 곱할 수 있다.
     * - [x] Money를 음수로 곱하면 BAD_REQUEST를 반환한다.
     */
    @DisplayName("Money 연산 시")
    @Nested
    class Operation {
        @DisplayName("두 Money를 더할 수 있다.")
        @Test
        void addTwoMoney() {
            // arrange
            Money money1 = Money.of(1000);
            Money money2 = Money.of(2000);

            // act
            Money result = money1.add(money2);

            // assert
            assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(3000));
        }

        @DisplayName("null Money와 더하기 시 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenAddingNullMoney() {
            // arrange
            Money money = Money.of(1000);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                money.add(null);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("추가 금액이 null 입니다.");
        }

        @DisplayName("두 Money를 뺄 수 있다.")
        @Test
        void subtractTwoMoney() {
            // arrange
            Money money1 = Money.of(3000);
            Money money2 = Money.of(1000);

            // act
            Money result = money1.subtract(money2);

            // assert
            assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(2000));
        }

        @DisplayName("더 큰 금액을 빼면 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenSubtractingLargerAmount() {
            // arrange
            Money money1 = Money.of(1000);
            Money money2 = Money.of(2000);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                money1.subtract(money2);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("금액은 음수가 될 수 없습니다.");
        }

        @DisplayName("null Money와 빼기 시 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenSubtractingNullMoney() {
            // arrange
            Money money = Money.of(1000);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                money.subtract(null);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("차감 금액이 null 입니다.");
        }

        @DisplayName("Money를 양수로 곱할 수 있다.")
        @Test
        void multiplyByPositiveNumber() {
            // arrange
            Money money = Money.of(1000);

            // act
            Money result = money.multiply(3);

            // assert
            assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(3000));
        }

        @DisplayName("Money를 0으로 곱할 수 있다.")
        @Test
        void multiplyByZero() {
            // arrange
            Money money = Money.of(1000);

            // act
            Money result = money.multiply(0);

            // assert
            assertThat(result.amount()).isEqualTo(BigDecimal.ZERO);
        }

        @DisplayName("Money를 음수로 곱하면 BAD_REQUEST를 반환한다.")
        @Test
        void fail_whenMultiplyByNegativeNumber() {
            // arrange
            Money money = Money.of(1000);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                money.multiply(-1);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("수량은 0 이상이어야 합니다.");
        }
    }
}
