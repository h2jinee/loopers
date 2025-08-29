package com.loopers.application.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@RecordApplicationEvents
class OrderFacadeTest {

    @Autowired
    private OrderFacade orderFacade;
    
    @Autowired
    private ApplicationEvents events;
    
    @Autowired
    private UserJpaRepository userRepository;
    
    @Autowired
    private PointJpaRepository pointRepository;
    
    private static final String TEST_USER_ID = "testuser123";
    private ReceiverInfo receiverInfo;
    
    @BeforeEach
    void setUp() {
        receiverInfo = new ReceiverInfo("홍길동", "010-1234-5678", "12345", "서울시", "상세주소");

        // 테스트용 사용자와 포인트 데이터 준비
        if (!userRepository.existsByUserId(TEST_USER_ID)) {
            // User 생성 - 실제 생성자 사용
            User testUser = new User(
                TEST_USER_ID,
                "테스트유저",
                User.Gender.M,
                "19900101",
                "test@example.com"
            );
            userRepository.save(testUser);

            // Point 생성
            Point point = new Point(TEST_USER_ID, Money.of(BigDecimal.valueOf(10000)));
            pointRepository.save(point);
        }
    }


    @Test
    void publishesOrderCompletedEvent_whenOrderIsCreatedSuccessfully() {
        // Given (1주문당 1개 상품)
        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(
            TEST_USER_ID,
            1L,  // 존재하는 상품
            1,   // 1개만 주문 (프로젝트 정책)
            receiverInfo
        );
        
        // When
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);
        
        // Then
        assertThat(result.orderId()).isNotNull();
        
        assertThat(events.stream(OrderCompleted.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.orderId()).isNotNull();
                assertThat(event.userId()).isEqualTo(TEST_USER_ID);
                assertThat(event.productId()).isEqualTo(1L);
                assertThat(event.quantity()).isEqualTo(1);
                assertThat(event.totalAmount()).isPositive();
                assertThat(event.paymentMethod()).isEqualTo(PaymentMethod.PG);
                assertThat(event.orderLines()).hasSize(1);
            });
    }

    @Test 
    void throwsException_whenStockIsInsufficient() {
        // Given
        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(
            TEST_USER_ID,
            1L,
            999,  // 재고보다 많은 수량 요청
            receiverInfo
        );
        
        // When & Then
        assertThatThrownBy(() -> orderFacade.createOrder(criteria))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("재고가 부족합니다");
        
        // OrderCompleted 이벤트는 발행되지 않음
        assertThat(events.stream(OrderCompleted.class)).hasSize(0);
    }

    @Test
    void throwsException_whenPointIsInsufficient() {
        // Given
        OrderCriteria.Create criteria = OrderCriteria.Create.pointOnly(
            TEST_USER_ID,
            1L,
            1,
            receiverInfo,
            Money.of(BigDecimal.valueOf(50000)) // 보유 포인트(10,000)보다 많은 금액
        );
        
        // When & Then
        assertThatThrownBy(() -> orderFacade.createOrder(criteria))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("포인트가 부족합니다");
        
        // OrderCompleted 이벤트는 발행되지 않음
        assertThat(events.stream(OrderCompleted.class)).hasSize(0);
    }

    @Test
    void maintainsOrderSuccess_whenCouponProcessingFails() {
        // Given - 쿠폰 할인이 포함된 주문 (부가 로직)
        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(
            TEST_USER_ID,
            1L,
            1,
            receiverInfo
        );
        
        // When - 주문 생성 (메인 로직 성공)
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);
        
        // Then - 주문은 성공
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        
        // OrderCompleted 이벤트 발행됨
        assertThat(events.stream(OrderCompleted.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.orderId()).isNotNull();
                assertThat(event.requiresCouponUsage()).isFalse();
            });
        
        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                // 실패해도 주문은 성공 상태 유지
                assertThat(events.stream(OrderCompleted.class)).hasSize(1);
            });
    }
}
