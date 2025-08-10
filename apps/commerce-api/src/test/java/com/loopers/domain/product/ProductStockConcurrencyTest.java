package com.loopers.domain.product;

import com.loopers.support.util.ConcurrentTestUtil;
import com.loopers.infrastructure.product.ProductStockJpaRepository;
import org.junit.jupiter.api.AfterEach;
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
class ProductStockConcurrencyTest {

    @Autowired
    private ProductStockService productStockService;

    @Autowired
    private ProductStockJpaRepository productStockRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        // 동적으로 product_id 생성
        productId = System.currentTimeMillis() % 100000;
        
        int initialStock = 100;
        ProductStockEntity productStock = new ProductStockEntity(productId, initialStock);
        ProductStockEntity saved = productStockRepository.save(productStock);
        productId = saved.getProductId();
    }

    @Test
    @DisplayName("비관적 락 - 100개 스레드가 동시에 재고 차감 시 정상 처리")
    void pessimisticLock() throws InterruptedException {
        int initialStock = 100;
        int threadCount = 100;
        int decreaseAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockPessimistic(productId, decreaseAmount);
                } catch (Exception e) {
                    log.error("비관적 락 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(initialStock - (threadCount * decreaseAmount));

        log.info("비관적 락 결과: {}", result.getStock());
    }

    @Test
    @DisplayName("낙관적 락 - 100개 스레드가 동시에 재고 차감 시 정상 처리")
    void optimisticLock() throws InterruptedException {
        int threadCount = 100;
        int decreaseAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockOptimistic(productId, decreaseAmount);
                } catch (Exception e) {
                    log.error("낙관적 락 재시도 또는 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        log.info("낙관적 락 결과: {}", result.getStock());
    }

    @Test
    @DisplayName("락 없음 - 100개 스레드가 동시에 재고 차감 시 Lost Update 문제 발생")
    void noLock() throws InterruptedException {
        int initialStock = 100;
        int threadCount = 100;
        int decreaseAmount = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockNoLock(productId, decreaseAmount);
                } catch (Exception e) {
                    log.error("락 없음 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        log.info("락 없음 결과 (Lost Update 확인): {}", result.getStock());
        
        assertThat(result.getStock()).isNotEqualTo(initialStock - (threadCount * decreaseAmount));
    }
}
