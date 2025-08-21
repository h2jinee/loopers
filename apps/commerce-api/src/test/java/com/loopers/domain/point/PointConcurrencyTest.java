package com.loopers.domain.point;

import com.loopers.application.point.PointCriteria;
import com.loopers.application.point.PointFacade;
import com.loopers.support.util.ConcurrentTestUtil;
import com.loopers.domain.common.Money;
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
class PointConcurrencyTest {

    @Autowired
    private PointFacade pointFacade;

    @Autowired
    private PointJpaRepository pointRepository;

    private String userId;

    @BeforeEach
    void setUp() {
        int initialPoint = 10000;
        userId = "test-user-" + System.currentTimeMillis();  // 동적 userId 생성
        Point point = new Point(userId, Money.of(initialPoint));
        pointRepository.save(point);
    }

    @Test
    @DisplayName("withLock 패턴 - 100개 스레드가 동시에 포인트 사용 시 정상 처리")
    void withLockPattern() throws InterruptedException {
        int initialPoint = 10000;
        int threadCount = 100;
        int useAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i + 1;
            tasks.add(() -> {
                try {
                    PointCommand.Use command = new PointCommand.Use(userId, Money.of(useAmount), orderId);
                    pointFacade.use(command);
                } catch (Exception e) {
                    log.error("withLock 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Point result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getBalance().amount().intValue()).isEqualTo(initialPoint - (threadCount * useAmount));

        log.info("withLock 결과: {}", result.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("포인트 충전 - 100개 스레드가 동시에 포인트 충전 시 정상 처리")
    void concurrentCharge() throws InterruptedException {
        int threadCount = 100;
        int chargeAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    PointCriteria.Charge criteria =
                        new PointCriteria.Charge(userId, (long)chargeAmount);
                    pointFacade.charge(criteria);
                } catch (Exception e) {
                    log.error("충전 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Point result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getBalance().amount().intValue()).isEqualTo(10000 + (threadCount * chargeAmount));
        log.info("충전 결과: {}", result.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("동시성 제어 검증 - 포인트 사용과 충전이 동시에 일어날 때 정합성 유지")
    void mixedOperations() throws InterruptedException {
        int initialPoint = 10000;
        int threadCount = 50;
        int operationAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        // 25개는 포인트 사용
        for (int i = 0; i < threadCount / 2; i++) {
            final long orderId = i + 1;
            tasks.add(() -> {
                try {
                    PointCommand.Use command = new PointCommand.Use(userId, Money.of(operationAmount), orderId);
                    pointFacade.use(command);
                } catch (Exception e) {
                    log.error("사용 실패: {}", e.getMessage());
                }
            });
        }

        // 25개는 포인트 충전
        for (int i = 0; i < threadCount / 2; i++) {
            tasks.add(() -> {
                try {
                    PointCriteria.Charge criteria = 
                        new PointCriteria.Charge(userId, (long)operationAmount);
                    pointFacade.charge(criteria);
                } catch (Exception e) {
                    log.error("충전 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Point result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        
        // 초기값 10000 - (25 * 100) + (25 * 100) = 10000
        assertThat(result.getBalance().amount().intValue()).isEqualTo(initialPoint);
        log.info("혼합 작업 결과: {}", result.getBalance().amount().intValue());
    }
}
