package com.loopers.domain.point;

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
    private PointService pointService;

    @Autowired
    private PointJpaRepository pointRepository;

    private String userId;

    @BeforeEach
    void setUp() {
        int initialPoint = 10000;
        userId = "test-user-" + System.currentTimeMillis();  // 동적 userId 생성
        PointEntity point = new PointEntity(userId, Money.of(initialPoint));
        pointRepository.save(point);
    }

    @Test
    @DisplayName("비관적 락 - 100개 스레드가 동시에 포인트 사용 시 정상 처리")
    void pessimisticLock() throws InterruptedException {
        int initialPoint = 10000;
        int threadCount = 100;
        int useAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i + 1;
            tasks.add(() -> {
                try {
                    PointCommand.Use command = new PointCommand.Use(userId, Money.of(useAmount), orderId);
                    pointService.usePointPessimistic(command);
                } catch (Exception e) {
                    log.error("비관적 락 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        PointEntity result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getBalance().amount().intValue()).isEqualTo(initialPoint - (threadCount * useAmount));

        log.info("비관적 락 결과: {}", result.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("낙관적 락 - 100개 스레드가 동시에 포인트 사용 시 정상 처리")
    void optimisticLock() throws InterruptedException {
        int threadCount = 100;
        int useAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i + 1;
            tasks.add(() -> {
                try {
                    PointCommand.Use command = new PointCommand.Use(userId, Money.of(useAmount), orderId);
                    pointService.usePointOptimistic(command);
                } catch (Exception e) {
                    log.error("낙관적 락 재시도 또는 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        PointEntity result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        log.info("낙관적 락 결과: {}", result.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("락 없음 - 100개 스레드가 동시에 포인트 사용 시 Lost Update 문제 발생")
    void noLock() throws InterruptedException {
        int initialPoint = 10000;
        int threadCount = 100;
        int useAmount = 100;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i + 1;
            tasks.add(() -> {
                try {
                    PointCommand.Use command = new PointCommand.Use(userId, Money.of(useAmount), orderId);
                    pointService.usePointNoLock(command);
                } catch (Exception e) {
                    log.error("락 없음 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        PointEntity result = pointRepository.findByUserId(userId).orElse(null);
        assertThat(result).isNotNull();
        log.info("락 없음 결과 (Lost Update 확인): {}", result.getBalance().amount().intValue());
        
        assertThat(result.getBalance().amount().intValue()).isNotEqualTo(initialPoint - (threadCount * useAmount));
    }
}
