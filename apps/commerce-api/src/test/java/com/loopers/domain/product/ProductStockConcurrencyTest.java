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

@SpringBootTest
class ProductStockConcurrencyTest {

    @Autowired
    private ProductStockService productStockService;

    @Autowired
    private ProductStockJpaRepository productStockRepository;

    private Long productId;
    private static final int INITIAL_STOCK = 100;
    private static final int THREAD_COUNT = 100;
    private static final int DECREASE_AMOUNT = 1;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        productStockRepository.deleteAll();
        
        // 동적으로 product_id 생성
        productId = System.currentTimeMillis() % 100000;
        
        ProductStockEntity productStock = new ProductStockEntity(productId, INITIAL_STOCK);
        ProductStockEntity saved = productStockRepository.save(productStock);
        productId = saved.getProductId();
    }

    @AfterEach
    void tearDown() {
        productStockRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락 - 100개 스레드가 동시에 재고 차감 시 정상 처리")
    void pessimisticLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockPessimistic(productId, DECREASE_AMOUNT);
                } catch (Exception e) {
                    System.out.println("비관적 락 실패: " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(INITIAL_STOCK - (THREAD_COUNT * DECREASE_AMOUNT));

        System.out.println("비관적 락 결과: " + result.getStock());
    }

    @Test
    @DisplayName("낙관적 락 - 100개 스레드가 동시에 재고 차감 시 정상 처리")
    void optimisticLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockOptimistic(productId, DECREASE_AMOUNT);
                } catch (Exception e) {
                    System.out.println("낙관적 락 재시도 또는 실패: " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        System.out.println("낙관적 락 결과: " + result.getStock());
    }

    @Test
    @DisplayName("락 없음 - 100개 스레드가 동시에 재고 차감 시 Lost Update 문제 발생")
    void noLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                try {
                    productStockService.decreaseStockNoLock(productId, DECREASE_AMOUNT);
                } catch (Exception e) {
                    System.out.println("락 없음 실패: " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity result = productStockRepository.findByProductId(productId).orElse(null);
        assertThat(result).isNotNull();
        System.out.println("락 없음 결과 (Lost Update 확인): " + result.getStock());
        
        assertThat(result.getStock()).isNotEqualTo(INITIAL_STOCK - (THREAD_COUNT * DECREASE_AMOUNT));
    }
}
