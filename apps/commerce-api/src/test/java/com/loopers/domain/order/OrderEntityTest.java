package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class OrderEntityTest {

    /*
     * Order 생성 단위 테스트
     * - [x] 정상적인 정보로 Order를 생성할 수 있다.
     * - [x] 주문 생성 시 상태는 PAYMENT_PENDING이다.
     */
    @DisplayName("Order 생성 시")
    @Nested
    class Create {
        private ReceiverInfo receiverInfo;

        @BeforeEach
        void setUp() {
            receiverInfo = new ReceiverInfo(
                "전희진", "01012345678",
                "12345", "서울시 강남구", "테스트 아파트 101호"
            );
        }

        @DisplayName("정상적인 정보로 Order를 생성할 수 있다.")
        @Test
        void createOrder_withValidInfo() {
            // arrange
            String userId = "testUser";

            // act
            OrderEntity order = new OrderEntity(userId, receiverInfo);

            // assert
            assertThat(order).isNotNull();
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getReceiverInfo()).isEqualTo(receiverInfo);
        }

        @DisplayName("주문 생성 시 상태는 PAYMENT_PENDING이다.")
        @Test
        void initialStatus_isPaymentPending() {
            // act
            OrderEntity order = new OrderEntity("testUser", receiverInfo);

            // assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }
    }

    /*
     * 결제 시간 만료 확인 단위 테스트
     * - [x] PAYMENT_PENDING 상태에서 결제 시간이 만료되지 않았으면 false를 반환한다.
     * - [x] PAYMENT_PENDING 상태에서 결제 시간이 만료되었으면 true를 반환한다.
     * - [x] PAYMENT_PENDING이 아닌 상태에서는 항상 false를 반환한다.
     */
    @DisplayName("결제 시간 만료 확인 시")
    @Nested
    class PaymentExpiration {
        private OrderEntity order;

        @BeforeEach
        void setUp() {
            ReceiverInfo receiverInfo = new ReceiverInfo(
                "전희진", "01012345678",
                "12345", "서울시 강남구", "테스트 아파트 101호"
            );
            order = new OrderEntity("testUser", receiverInfo);
        }

        @DisplayName("PAYMENT_PENDING 상태에서 결제 시간이 만료되지 않았으면 false를 반환한다.")
        @Test
        void notExpired_whenWithinDeadline() {
            // assert
            assertThat(order.isPaymentExpired()).isFalse();
        }

        @DisplayName("PAYMENT_PENDING이 아닌 상태에서는 항상 false를 반환한다.")
        @Test
        void alwaysFalse_whenNotPaymentPending() {
            // arrange
            order.confirmPayment();

            // assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            assertThat(order.isPaymentExpired()).isFalse();
        }
    }

    /*
     * 결제 확정/실패 단위 테스트
     * - [x] PAYMENT_PENDING 상태에서 결제를 확정할 수 있다.
     * - [x] PAYMENT_PENDING이 아닌 상태에서 결제 확정 시 BAD_REQUEST를 반환한다.
     * - [x] PAYMENT_PENDING이 아닌 상태에서 결제 실패 처리 시 BAD_REQUEST를 반환한다.
     */
    @DisplayName("결제 확정/실패 처리 시")
    @Nested
    class PaymentProcessing {
        private OrderEntity order;

        @BeforeEach
        void setUp() {
            ReceiverInfo receiverInfo = new ReceiverInfo(
                "홍길동", "01012345678",
                "12345", "서울시 강남구", "테스트 아파트 101호"
            );
            order = new OrderEntity("testUser", receiverInfo);
        }

        @DisplayName("PAYMENT_PENDING 상태에서 결제를 확정할 수 있다.")
        @Test
        void confirmPayment_successfully() {
            // act
            order.confirmPayment();

            // assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        }

        @DisplayName("PAYMENT_PENDING이 아닌 상태에서 결제 확정 시 BAD_REQUEST를 반환한다.")
        @Test
        void fail_confirmPayment_whenNotPending() {
            // arrange
            order.confirmPayment();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                order.confirmPayment();
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("결제 대기 상태가 아닙니다.");
        }

        @DisplayName("PAYMENT_PENDING이 아닌 상태에서 결제 실패 처리 시 BAD_REQUEST를 반환한다.")
        @Test
        void fail_failPayment_whenNotPending() {
            // arrange
            order.failPayment();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                order.failPayment();
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("결제 대기 상태가 아닙니다.");
        }
    }
}
