package com.loopers.interfaces.api.payment;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.loopers.application.event.payment.PaymentEvent;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResultCommand;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.PointService;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PaymentResponse;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;

@SpringBootTest
@RecordApplicationEvents
class PaymentV1ApiE2ETest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ApplicationEvents events;  // 이벤트 기록

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentGatewayClient paymentGatewayClient;

    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // 주문 조회 모킹
        Order mockOrder = mock(Order.class);
        when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
        when(mockOrder.getOrderLines()).thenReturn(Collections.emptyList());
        when(orderService.findById(TEST_ORDER_ID)).thenReturn(mockOrder);
    }

    private static final String TEST_USER_ID = "user123";
    private static final Long TEST_ORDER_ID = 100001L;

    @Test
    @DisplayName("PG 결제 성공 플로우")
    void pg_payment_success_flow() {
        // given
        String transactionId = "TXN_PG_" + System.currentTimeMillis();

        PaymentDto.V1.Initiate.Request request = new PaymentDto.V1.Initiate.Request(
            TEST_ORDER_ID,
            BigDecimal.valueOf(10000),
            PaymentMethod.PG,
            "SAMSUNG",
            "1234-5678-9012-3456"
        );

        when(paymentGatewayClient.send(any(), any()))
            .thenReturn(createSuccessResponse(transactionId));

        // when
        PaymentResult result = paymentFacade.initiatePayment(TEST_USER_ID, request);

        // then
        assertThat(result.isPending()).isTrue();
        assertThat(result.transactionId()).isNotNull();
    }

    @Test
    @DisplayName("포인트 결제 성공 플로우")
    void point_payment_success_flow() {
        // given
        PaymentDto.V1.Initiate.Request request = new PaymentDto.V1.Initiate.Request(
            TEST_ORDER_ID,
            BigDecimal.valueOf(5000),
            PaymentMethod.POINT,
            null,
            null
        );

        doNothing().when(pointService).usePoint(any(), any(), any());

        // when
        PaymentResult result = paymentFacade.initiatePayment(TEST_USER_ID, request);

        // then
        assertThat(result.isSuccess()).isTrue();
        verify(pointService).usePoint(eq(TEST_USER_ID), any(Money.class), eq(TEST_ORDER_ID));
    }

    @Test
    @DisplayName("포인트 부족으로 결제 실패")
    void point_payment_insufficient_balance() {
        // given
        PaymentDto.V1.Initiate.Request request = new PaymentDto.V1.Initiate.Request(
            TEST_ORDER_ID,
            BigDecimal.valueOf(50000),
            PaymentMethod.POINT,
            null,
            null
        );

        doThrow(new CoreException(ErrorType.BAD_REQUEST, "포인트 부족"))
            .when(pointService).usePoint(any(), any(), any());

        // when & then
        assertThatThrownBy(() -> paymentFacade.initiatePayment(TEST_USER_ID, request))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("포인트 부족");
    }

    @Test
    @DisplayName("PG 콜백 처리 - 성공")
    void pg_callback_success() {
        // given
        String transactionId = "TXN_CALLBACK_" + System.currentTimeMillis();
        createPendingPayment(transactionId);

        PaymentResultCommand command = PaymentResultCommand.basicResult(
            transactionId, TEST_ORDER_ID, true, null
        );

        // when - 비동기 실행
        paymentFacade.processPaymentResult(command);

        // then
        await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                Payment payment = paymentRepository.findByTransactionId(transactionId)
                    .orElseThrow();
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            });
    }

    @Test
    @DisplayName("이벤트 발행 검증")
    void payment_events_are_published() {
        // given
        PaymentDto.V1.Initiate.Request request = new PaymentDto.V1.Initiate.Request(
            TEST_ORDER_ID,
            BigDecimal.valueOf(5000),
            PaymentMethod.POINT,
            null,
            null
        );

        doNothing().when(pointService).usePoint(any(), any(), any());

        // when
        paymentFacade.initiatePayment(TEST_USER_ID, request);

        // then
        assertThat(events.stream(PaymentEvent.Completed.class)).hasSize(1);
    }

    // 헬퍼 메서드들
    private ApiResponse<PaymentResponse> createSuccessResponse(String transactionId) {
        PaymentResponse response = new PaymentResponse(
            transactionId,
            "SUCCESS",
            null
        );
        return ApiResponse.success(response);
    }

    private void createPendingPayment(String transactionId) {
        Payment payment = Payment.createPgPayment(
            TEST_ORDER_ID,
            TEST_USER_ID,
            Money.of(BigDecimal.valueOf(10000)),
            transactionId
        );
        paymentRepository.save(payment);
    }
}
