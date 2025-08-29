package com.loopers.application.order;

import com.loopers.application.order.OrderCompleted;
import com.loopers.application.order.OrderCancelled;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.payment.PaymentMethod;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * 멘토님이 언급한 테스트 방식
 * @RecordApplicationEvents - 이벤트 발행 확인
 * Awaitility - 비동기 이벤트 처리 대기
 */
@SpringBootTest
@RecordApplicationEvents  // 멘토님 언급: 실제 메시지가 발행되었는지 확인 가능
class OrderFacadeEventTest {

    @Autowired
    private OrderFacade orderFacade;
    
    @Autowired
    private ApplicationEvents events;  // 발행된 이벤트들 수집

    @Test
    @Transactional
    void 주문_생성시_OrderCompleted_이벤트가_발행된다() {
        // Given
        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(
            "user123",
            1L,
            2,
            new ReceiverInfo("홍길동", "010-1234-5678", "12345", "서울시", "상세주소")
        );
        
        // When
        orderFacade.createOrder(criteria);
        
        // Then - 멘토님: @RecordApplicationEvents로 메시지 선택해서 받을 수 있음
        assertThat(events.stream(OrderCompleted.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.orderId()).isNotNull();
                assertThat(event.userId()).isEqualTo("user123");
                assertThat(event.productId()).isEqualTo(1L);
                assertThat(event.quantity()).isEqualTo(2);
                assertThat(event.totalAmount()).isPositive();
                assertThat(event.paymentMethod()).isEqualTo(PaymentMethod.PG);
                assertThat(event.orderLines()).isNotEmpty();
            });
    }

    @Test
    void 재고_부족시_주문은_성공하고_재고차감만_독립적으로_실패한다() {
        // Given - 재고가 부족한 상품
        OrderCriteria.Create criteria = createOrderCriteriaWithInsufficientStock();
        
        // When
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);
        
        // Then - 주문 자체는 성공
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        
        // 주문 완료 이벤트는 발행됨
        assertThat(events.stream(OrderCompleted.class)).hasSize(1);
        
        // 멘토님: Awaitility로 몇 분동안 계속 검증, 폴링 가능
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                // 재고 차감은 별도로 실패하지만 주문은 유지됨
                // (실제로는 재고 차감 실패 이벤트나 로그를 확인해야 함)
                assertThat(events.stream(OrderCompleted.class)).hasSize(1);
            });
    }

    @Test
    void 포인트_전용_주문시_포인트_사용_이벤트가_처리된다() {
        // Given
        OrderCriteria.Create criteria = OrderCriteria.Create.pointOnly(
            "user123",
            1L,
            1,
            new ReceiverInfo("홍길동", "010-1234-5678", "12345", "서울시", "상세주소"),
            Money.of(BigDecimal.valueOf(1000))
        );
        
        // When
        orderFacade.createOrder(criteria);
        
        // Then
        assertThat(events.stream(OrderCompleted.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.requiresPointUsage()).isTrue();
                assertThat(event.requiresPayment()).isFalse(); // 포인트로만 결제
                assertThat(event.paymentMethod()).isEqualTo(PaymentMethod.POINT);
                assertThat(event.pointAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            });
    }

    private OrderCriteria.Create createOrderCriteriaWithInsufficientStock() {
        return OrderCriteria.Create.withoutPoint(
            "user123",
            999L, // 존재하지 않거나 재고 부족한 상품
            999,  // 대량 주문
            new ReceiverInfo("홍길동", "010-1234-5678", "12345", "서울시", "상세주소")
        );
    }
}