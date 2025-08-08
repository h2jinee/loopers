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
    private static final int INITIAL_STOCK = 50;
    private static final int INITIAL_POINT = 50000;
    private static final int THREAD_COUNT = 50;
    private static final int ORDER_QUANTITY = 1;
    private static final int PRODUCT_PRICE = 1000;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        productStockRepository.deleteAll();
        productRepository.deleteAll();
        pointRepository.deleteAll();
        
        userId = "test-user";
        
        ProductEntity product = new ProductEntity(
            1L, "테스트 상품", Money.of(PRODUCT_PRICE), "테스트 설명",
            ProductStatus.AVAILABLE, 2024, Money.of(0)  // 배송료 0
        );
        ProductEntity savedProduct = productRepository.save(product);
        productId = savedProduct.getId();

        ProductStockEntity productStock = new ProductStockEntity(productId, INITIAL_STOCK);
        productStockRepository.save(productStock);

        PointEntity point = new PointEntity(userId, Money.of(INITIAL_POINT));
        pointRepository.save(point);
    }

    @AfterEach
    void tearDown() {
        productStockRepository.deleteAll();
        productRepository.deleteAll();
        pointRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락 - 50개 스레드가 동시에 주문 생성 시 정상 처리")
    void pessimisticLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int idx = i;
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, ORDER_QUANTITY, receiverInfo);
                    orderFacade.createOrderPessimistic(criteria);
                    successCount.incrementAndGet();
                    System.out.println("비관적 락 주문 성공: " + idx);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("비관적 락 주문 실패 " + idx + ": " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        System.out.println("=== 비관적 락 테스트 결과 ===");
        System.out.println("성공 건수: " + successCount.get());
        System.out.println("실패 건수: " + failCount.get());
        System.out.println("남은 재고: " + stockResult.getStock());
        System.out.println("남은 포인트: " + pointResult.getBalance().amount().intValue());
        System.out.println("초기 재고: " + INITIAL_STOCK + ", 초기 포인트: " + INITIAL_POINT);
        System.out.println("예상 재고: " + (INITIAL_STOCK - (THREAD_COUNT * ORDER_QUANTITY)));
        System.out.println("예상 포인트: " + (INITIAL_POINT - (THREAD_COUNT * PRODUCT_PRICE * ORDER_QUANTITY)));
        System.out.println("===========================");

        assertThat(stockResult.getStock()).isEqualTo(INITIAL_STOCK - (THREAD_COUNT * ORDER_QUANTITY));
        assertThat(pointResult.getBalance().amount().intValue()).isEqualTo(INITIAL_POINT - (THREAD_COUNT * PRODUCT_PRICE * ORDER_QUANTITY));
    }

    @Test
    @DisplayName("낙관적 락 - 50개 스레드가 동시에 주문 생성 시 정상 처리")
    void optimisticLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, ORDER_QUANTITY, receiverInfo);
                    orderFacade.createOrderOptimistic(criteria);
                } catch (Exception e) {
                    System.out.println("낙관적 락 주문 재시도 또는 실패: " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        System.out.println("낙관적 락 - 남은 재고: " + stockResult.getStock());
        System.out.println("낙관적 락 - 남은 포인트: " + pointResult.getBalance().amount().intValue());
    }

    @Test
    @DisplayName("락 없음 - 50개 스레드가 동시에 주문 생성 시 Lost Update 문제 발생")
    void noLock() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            tasks.add(() -> {
                try {
                    ReceiverInfo receiverInfo = new ReceiverInfo("테스트", "010-1234-5678", "12345", "서울시", "상세주소");
                    OrderCriteria.Create criteria = new OrderCriteria.Create(userId, productId, ORDER_QUANTITY, receiverInfo);
                    orderFacade.createOrderNoLock(criteria);
                } catch (Exception e) {
                    System.out.println("락 없음 주문 실패: " + e.getMessage());
                }
            });
        }

        ConcurrentTestUtil.executeAsyncWithTasks(tasks);

        ProductStockEntity stockResult = productStockRepository.findByProductId(productId).orElse(null);
        PointEntity pointResult = pointRepository.findByUserId(userId).orElse(null);

        assertThat(stockResult).isNotNull();
        assertThat(pointResult).isNotNull();

        System.out.println("락 없음 - 남은 재고 (Lost Update 확인): " + stockResult.getStock());
        System.out.println("락 없음 - 남은 포인트 (Lost Update 확인): " + pointResult.getBalance().amount().intValue());

        assertThat(stockResult.getStock()).isNotEqualTo(INITIAL_STOCK - (THREAD_COUNT * ORDER_QUANTITY));
        assertThat(pointResult.getBalance().amount().intValue()).isNotEqualTo(INITIAL_POINT - (THREAD_COUNT * PRODUCT_PRICE * ORDER_QUANTITY));
    }
}
