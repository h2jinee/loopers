package com.loopers.application.order;

import com.loopers.domain.common.Money;
import com.loopers.domain.event.Outbox;
import com.loopers.domain.event.OutboxRepository;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderFacadeTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PointJpaRepository pointRepository;

    private static final String TEST_USER_ID = "testuser123";
    private ReceiverInfo receiverInfo;
    private int initialOutboxCount;

    @BeforeEach
    void setUp() {
        receiverInfo = new ReceiverInfo("홍길동", "010-1234-5678", "12345", "서울시", "상세주소");

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

        // 현재 Outbox 이벤트 개수 저장
        initialOutboxCount = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        ).size();
    }

    @Test
    void savesOrderCreatedEventToOutbox_whenOrderIsCreatedSuccessfully() {
        // Given
        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(
            TEST_USER_ID,
            1L,
            1,
            receiverInfo
        );

        // When
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);

        // Then
        assertThat(result.orderId()).isNotNull();

        // Outbox에 이벤트가 저장되었는지 확인
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );

        assertThat(outboxEvents).hasSize(initialOutboxCount + 1);

        Outbox lastEvent = outboxEvents.get(outboxEvents.size() - 1);
        assertThat(lastEvent.getEventType()).isEqualTo("Created");
        assertThat(lastEvent.getTopic()).isEqualTo("order-events");
        assertThat(lastEvent.getAggregateId()).isEqualTo(result.orderId().toString());
    }

    @Test
    void createsOrderSuccessfully_evenWithInsufficientPoint() {
        // Given
        Point point = pointRepository.findByUserId(TEST_USER_ID).orElseThrow();
        BigDecimal currentBalance = point.getBalance().amount();
        BigDecimal requestAmount = currentBalance.add(BigDecimal.valueOf(1000));

        OrderCriteria.Create criteria = OrderCriteria.Create.pointOnly(
            TEST_USER_ID,
            1L,
            1,
            receiverInfo,
            Money.of(requestAmount)
        );

        // When - 주문 생성 성공
        OrderResult.CreateResult result = orderFacade.createOrder(criteria);

        // Then - PENDING 상태
        assertThat(result.orderId()).isNotNull();

        // Outbox에 이벤트가 저장됨
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );
        assertThat(outboxEvents).hasSize(initialOutboxCount + 1);
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

        // Outbox에 이벤트가 저장되지 않았는지 확인
        List<Outbox> outboxEvents = outboxRepository.findByStatusOrderByCreatedAt(
            Outbox.OutboxStatus.PENDING,
            PageRequest.of(0, 100)
        );
        assertThat(outboxEvents).hasSize(initialOutboxCount);
    }
}
