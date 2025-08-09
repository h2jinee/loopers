package com.loopers.domain.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.support.util.ConcurrentTestUtil;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductStockEntity;
import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
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
class OrderConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private ProductStockJpaRepository productStockRepository;

    @Autowired
    private PointJpaRepository pointRepository;

    private Long productId;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "test-user-" + System.currentTimeMillis();  // 동적으로 생성
        
        int initialStock = 50;
        int initialPoint = 50000;
        int productPrice = 1000;
        
        ProductEntity product = new ProductEntity(
            1L, "테스트 상품", Money.of(productPrice), "테스트 설명",
            ProductStatus.AVAILABLE, 2024, Money.of(0)  // 배송료 0
        );
        ProductEntity savedProduct = productRepository.save(product);
        productId = savedProduct.getId();

        ProductStockEntity productStock = new ProductStockEntity(productId, initialStock);
        productStockRepository.save(productStock);

        PointEntity point = new PointEntity(userId, Money.of(initialPoint));
        pointRepository.save(point);
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        if (productId != null) {
            productRepository.deleteById(productId);
            productStockRepository.deleteById(productId);
        }
        if (userId != null) {
            PointEntity point = pointRepository.findByUserId(userId).orElse(null);
            if (point != null) {
                pointRepository.delete(point);
            }
        }
    }

    @Test
    @DisplayName("비관적 락 - 50개 스레드가 동시에 주문 생성 시 정상 처리")
    void pessimisticLock() throws InterruptedException {
        int initialStock = 50;
        int initialPoint = 50000;
        int threadCount = 50;
        int orderQuantity = 1;
        int productPrice = 1000;
        
        List<Runnable> tasks = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
			tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, orderQuantity, receiverInfo);
                    orderFacade.createOrderPessimistic(criteria);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("성공 건수: {}", successCount.get());
        log.info("실패 건수: {}", failCount.get());
        log.info("남은 재고: {}", stockResult.getStock());
        log.info("남은 포인트: {}", pointResult.getBalance().amount().intValue());
        log.info("초기 재고: {}, 초기 포인트: {}", initialStock, initialPoint);
        log.info("예상 재고: {}", (initialStock - (threadCount * orderQuantity)));
        log.info("예상 포인트: {}", (initialPoint - (threadCount * productPrice * orderQuantity)));
        log.info("===========================");

        assertThat(stockResult.getStock()).isEqualTo(initialStock - (threadCount * orderQuantity));
        assertThat(pointResult.getBalance().amount().intValue()).isEqualTo(initialPoint - (threadCount * productPrice * orderQuantity));
    }

    @Test
    @DisplayName("낙관적 락 - 50개 스레드가 동시에 주문 생성 시 정상 처리")
    void optimisticLock() throws InterruptedException {
        int threadCount = 50;
        int orderQuantity = 1;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, orderQuantity, receiverInfo);
                    orderFacade.createOrderOptimistic(criteria);
                } catch (Exception e) {
                    log.error("낙관적 락 주문 재시도 또는 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        log.info("낙관적 락 - 남은 재고: {}", stockResult.getStock());
        log.info("낙관적 락 - 남은 포인트: {}", pointResult.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("락 없음 - 50개 스레드가 동시에 주문 생성 시 Lost Update 문제 발생")
    void noLock() throws InterruptedException {
        int initialStock = 50;
        int initialPoint = 50000;
        int threadCount = 50;
        int orderQuantity = 1;
        int productPrice = 1000;
        
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, orderQuantity, receiverInfo);
                    orderFacade.createOrderNoLock(criteria);
                } catch (Exception e) {
                    log.error("락 없음 주문 실패: {}", e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        log.info("락 없음 - 남은 재고 (Lost Update 확인): {}", stockResult.getStock());
        log.info("락 없음 - 남은 포인트 (Lost Update 확인): {}", pointResult.getBalance().amount().intValue());

        assertThat(stockResult.getStock()).isNotEqualTo(initialStock - (threadCount * orderQuantity));
        assertThat(pointResult.getBalance().amount().intValue()).isNotEqualTo(initialPoint - (threadCount * productPrice * orderQuantity));
    }
}
