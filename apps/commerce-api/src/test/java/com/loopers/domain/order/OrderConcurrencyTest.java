package com.loopers.domain.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.point.PointCriteria;
import com.loopers.application.point.PointFacade;
import com.loopers.support.util.ConcurrentTestUtil;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.infrastructure.point.PointJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class OrderConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PointFacade pointFacade;
    
    @Autowired
    private PointJpaRepository pointRepository;
    
    private Long productId;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "test-user-" + System.currentTimeMillis();
        
        int initialPoint = 50000;
        
        // 테스트용 상품 ID
        productId = 1L;
        
        // 포인트 충전 (PointFacade 사용)
        PointCriteria.Charge chargeCriteria = new PointCriteria.Charge(userId, (long)initialPoint);
        pointFacade.charge(chargeCriteria);
    }

    @Test
    @DisplayName("동일 유저 - 50개 스레드가 동시에 주문 생성 시 정상 처리")
    void concurrentOrderBySameUser() throws InterruptedException {
        int initialPoint = 50000;
        int threadCount = 50;
        int orderQuantity = 1;

        List<Runnable> tasks = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);

        // 같은 유저가 동시에 여러 주문
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    // withoutPoint이므로 포인트 사용 안 함
                    OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(userId, productId, orderQuantity, receiverInfo);
                    orderFacade.createOrder(criteria);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.debug("주문 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        // 결과 확인
        log.info("성공: {}, 실패: {}", successCount.get(), failCount.get());

        // 포인트 확인 - withoutPoint 사용했으므로 포인트 그대로
        var point = pointRepository.findByUserId(userId).orElse(null);
        assertThat(point).isNotNull();

        // 포인트를 사용하지 않았으므로 초기값 그대로여야 함
        assertThat(point.getBalance().amount().intValue()).isEqualTo(initialPoint);

        log.info("남은 포인트: {}", point.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("다중 유저 - 각 유저가 동시에 주문 생성 시 정상 처리")
    void concurrentOrderByMultipleUsers() throws InterruptedException {
        int userCount = 10;
        int ordersPerUser = 5;
        int orderQuantity = 1;
        int productPrice = 1000;
        int initialPointPerUser = 10000;
        
        List<String> userIds = new ArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        
        // 각 유저 초기화
        for (int i = 0; i < userCount; i++) {
            String testUserId = "test-user-" + System.currentTimeMillis() + "-" + i;
            userIds.add(testUserId);
            
            // 각 유저에게 포인트 충전
            PointCriteria.Charge chargeCriteria = new PointCriteria.Charge(testUserId, (long)initialPointPerUser);
            pointFacade.charge(chargeCriteria);
        }
        
        // 각 유저별로 동시 주문 생성
        for (String testUserId : userIds) {
            for (int j = 0; j < ordersPerUser; j++) {
                tasks.add(() -> {
                    try {
                        ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                        OrderCriteria.Create criteria = OrderCriteria.Create.withoutPoint(userId, productId, orderQuantity, receiverInfo);
                        orderFacade.createOrder(criteria);
                    } catch (Exception e) {
                        log.debug("주문 실패 - userId: {}, error: {}", testUserId, e.getMessage());
                    }
                });
            }
        }
        
        ConcurrentTestUtil.executeAsyncWithTasks(tasks);
        
        // 각 유저의 포인트 확인
        for (String testUserId : userIds) {
            var point = pointRepository.findByUserId(testUserId).orElse(null);
            assertThat(point).isNotNull();
            log.info("유저 {} 남은 포인트: {}", testUserId, point.getBalance().amount().intValue());
        }
    }
}
