package com.loopers.domain.stock;

import com.loopers.application.stock.StockFacade;
import com.loopers.domain.common.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.infrastructure.product.ProductJpaRepository;
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
    
    @Autowired
    private ProductJpaRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        // 1. Product 생성
        Product product = new Product(
            1L, // brandId
            "테스트 상품 " + System.nanoTime(),
            Money.of(10000), // price
            "테스트 설명", // description
            ProductStatus.AVAILABLE, // status
            2024, // releaseYear
            Money.of(0) // shippingFee
        );
        Product savedProduct = productRepository.save(product);
        productId = savedProduct.getId();
        
        // 2. Stock 생성
        int initialStock = 100;
        Stock stock = new Stock(productId, initialStock);
        stockRepository.save(stock);
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리 (순서 중요: Stock 먼저 삭제)
        stockRepository.deleteAll();
        productRepository.deleteAll();
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
