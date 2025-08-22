package com.loopers.domain.stock;

import com.loopers.application.stock.StockFacade;
import com.loopers.infrastructure.stock.StockJpaRepository;
import com.loopers.support.util.ConcurrentTestUtil;
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

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        stockRepository.deleteAll();
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
                    stockFacade.decreaseStock(productId, decreaseAmount);
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
}
