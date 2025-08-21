package com.loopers.domain.stock;

import com.loopers.application.stock.StockFacade;
import com.loopers.infrastructure.stock.StockJpaRepository;
import com.loopers.support.util.ConcurrentTestUtil;
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
class StockConcurrencyTest {

    @Autowired
    private StockFacade stockFacade;

    @Autowired
    private StockJpaRepository stockRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        // 동적으로 product_id 생성
        productId = System.currentTimeMillis() % 100000;
        
        int initialStock = 100;
        Stock stock = new Stock(productId, initialStock);
        Stock saved = stockRepository.save(stock);
        productId = saved.getProductId();
    }

    @Test
    @DisplayName("withLock 패턴 - 100개 스레드가 동시에 재고 차감 시 정상 처리")
    void withLockPattern() throws InterruptedException {
        int initialStock = 100;
        int threadCount = 100;
        int decreaseAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    stockFacade.decreaseStock(productId, decreaseAmount);  // Facade 사용으로 트랜잭션과 락 적용
                } catch (Exception e) {
                    log.error("withLock 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Stock result = stockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(initialStock - (threadCount * decreaseAmount));

        log.info("withLock 결과: {}", result.getQuantity());
    }

    @Test
    @DisplayName("재고 증가 - 100개 스레드가 동시에 재고 증가 시 정상 처리")
    void concurrentIncrease() throws InterruptedException {
        int initialStock = 100;
        int threadCount = 100;
        int increaseAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    stockFacade.increaseStock(productId, increaseAmount);
                } catch (Exception e) {
                    log.error("재고 증가 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Stock result = stockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(initialStock + (threadCount * increaseAmount));
        log.info("재고 증가 결과: {}", result.getQuantity());
    }

    @Test
    @DisplayName("동시성 제어 검증 - 재고 차감과 증가가 동시에 일어날 때 정합성 유지")
    void mixedOperations() throws InterruptedException {
        int initialStock = 100;
        int threadCount = 50;
        int operationAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        // 25개는 재고 차감
        for (int i = 0; i < threadCount / 2; i++) {
            tasks.add(() -> {
                try {
                    stockFacade.decreaseStock(productId, operationAmount);
                } catch (Exception e) {
                    log.error("차감 실패: {}", e.getMessage());
                }
            });
        }

        // 25개는 재고 증가
        for (int i = 0; i < threadCount / 2; i++) {
            tasks.add(() -> {
                try {
                    stockFacade.increaseStock(productId, operationAmount);
                } catch (Exception e) {
                    log.error("증가 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        Stock result = stockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        
        // 초기값 100 - (25 * 1) + (25 * 1) = 100
        assertThat(result.getQuantity()).isEqualTo(initialStock);
        log.info("혼합 작업 결과: {}", result.getQuantity());
    }
}
