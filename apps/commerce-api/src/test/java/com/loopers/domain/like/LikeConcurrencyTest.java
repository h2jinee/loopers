package com.loopers.domain.like;

import com.loopers.support.util.ConcurrentTestUtil;
import com.loopers.domain.product.ProductCountCommand;
import com.loopers.domain.product.ProductCountEntity;
import com.loopers.domain.product.ProductCountService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.domain.common.Money;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductCountJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableRetry
@SpringBootTest
class LikeConcurrencyTest {

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private ProductCountJpaRepository productCountJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private TestLikeFacade testLikeFacade;

	private Long productId;

    @BeforeEach
    void setUp() {
        // 상품 생성 (동적 ID 생성)
        ProductEntity product = new ProductEntity(
            System.currentTimeMillis() % 10000,  // 동적 brandId
            "테스트 상품",
            Money.of(10000),
            "테스트 설명",
            ProductStatus.AVAILABLE,
            2024,
            Money.of(0)
        );
        ProductEntity savedProduct = productJpaRepository.save(product);
		productId = savedProduct.getId();

        // 상품 카운트 엔티티 초기화
        ProductCountEntity productCount = new ProductCountEntity(productId);
        productCountJpaRepository.save(productCount);
        
        log.info("테스트 셋업 완료 - 상품 ID: {}", productId);
    }

    @Test
    @DisplayName("비관적 락 - 100명이 동시에 좋아요 추가 시 정상 처리")
    void pessimisticLock_likeAdd() throws InterruptedException {
        // given
		int THREAD_COUNT = 100;

        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String userId = "user" + i;
            final int index = i;
            tasks.add(() -> {
                try {
                    log.debug("[{}] 좋아요 추가 시작 - userId: {}", index, userId);
                    boolean success = testLikeFacade.addLikePessimistic(userId, productId);
                    if (success) {
                        successCount.incrementAndGet();
                        log.debug("[{}] 좋아요 추가 성공 - userId: {}", index, userId);
                    } else {
                        duplicateCount.incrementAndGet();
                        log.debug("[{}] 중복 좋아요 - userId: {}", index, userId);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("[{}] 비관적 락 좋아요 추가 실패 - userId: {}, error: {}", index, userId, e.getMessage());
                }
            });
        }

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeAsyncWithTasks(tasks);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        Thread.sleep(1000); // 모든 트랜잭션이 커밋될 때까지 대기
        
        Long likeCount = likeJpaRepository.countByProductId(productId);
        ProductCountEntity productCount = productCountJpaRepository.findByProductId(productId).orElse(null);

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("요청: {}, 성공: {}, 중복: {}, 실패: {}", THREAD_COUNT, successCount.get(), duplicateCount.get(), failCount.get());
        log.info("최종 좋아요 수: {}", likeCount);
        log.info("카운트 엔티티: {}", productCount != null ? productCount.getLikeCount() : 0);
        
        // 실제 좋아요 수와 성공 카운트가 일치해야 함
        assertThat(likeCount).isEqualTo(Long.valueOf(successCount.get()));
        assertThat(productCount).isNotNull();
        assertThat(productCount.getLikeCount()).isEqualTo(likeCount);

        // 모든 요청이 처리되어야 함
        assertThat(successCount.get() + duplicateCount.get() + failCount.get()).isEqualTo(THREAD_COUNT);
    }

    @Test
    @DisplayName("낙관적 락 - 100명이 동시에 좋아요 추가 시 정상 처리")
    void optimisticLock_likeAdd() throws InterruptedException {
        // given
		int THREAD_COUNT = 100;
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger retryCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String userId = "user" + i;
            tasks.add(() -> {
                try {
                    boolean success = testLikeFacade.addLikeOptimistic(userId, productId);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        duplicateCount.incrementAndGet();
                    }
                } catch (ObjectOptimisticLockingFailureException e) {
                    retryCount.incrementAndGet();
                    log.error("낙관적 락 재시도: {}", e.getMessage());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("낙관적 락 좋아요 추가 실패: {}", e.getMessage());
                }
            });
        }

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeAsyncWithTasks(tasks);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        Long likeCount = likeJpaRepository.countByProductId(productId);
        ProductCountEntity productCount = productCountJpaRepository.findByProductId(productId).orElse(null);

        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("요청: {}, 성공: {}, 중복: {}, 재시도: {}, 실패: {}", THREAD_COUNT, successCount.get(), duplicateCount.get(), retryCount.get(), failCount.get());
        log.info("최종 좋아요 수: {}", likeCount);
        log.info("카운트 엔티티: {}", productCount != null ? productCount.getLikeCount() : 0);
        
        assertThat(likeCount).isEqualTo(Long.valueOf(successCount.get()));
        assertThat(productCount).isNotNull();
        assertThat(productCount.getLikeCount()).isEqualTo(likeCount);
    }

    @Test
    @DisplayName("락 없음 - 100명이 동시에 좋아요 추가 시 Lost Update 문제 발생")
    void noLock_likeAdd() throws InterruptedException {
        // given
		int THREAD_COUNT = 100;
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String userId = "user" + i;
            tasks.add(() -> {
                try {
                    boolean success = testLikeFacade.addLikeNoLock(userId, productId);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        duplicateCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("락 없음 좋아요 추가 실패: {}", e.getMessage());
                }
            });
        }

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeAsyncWithTasks(tasks);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        Long likeCount = likeJpaRepository.countByProductId(productId);
        ProductCountEntity productCount = productCountJpaRepository.findByProductId(productId).orElse(null);

        log.info("=== 락 없음 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("요청: {}, 성공: {}, 중복: {}, 실패: {}", THREAD_COUNT, successCount.get(), duplicateCount.get(), failCount.get());
        log.info("실제 좋아요 수: {}, 카운트 엔티티: {}", likeCount, productCount != null ? productCount.getLikeCount() : 0);
        
        assertThat(productCount).isNotNull();
        assertThat(likeCount).isEqualTo(Long.valueOf(successCount.get()));
        
        if (productCount.getLikeCount() < likeCount) {
            log.info("Lost Update 발생! 실제: {}, 카운트: {}", likeCount, productCount.getLikeCount());
        }
        
        // 락이 없으므로 카운트가 실제보다 적을 수 있음
        assertThat(productCount.getLikeCount()).isLessThanOrEqualTo(likeCount);
    }

    @Test
    @DisplayName("동일 사용자가 좋아요/싫어요 토글 시 정상 처리")
    void toggleLike() throws InterruptedException {
        // given
        String userId = "testUser";
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 10번 토글 (좋아요 추가 -> 삭제 반복)
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                try {
                    testLikeFacade.toggleLikePessimistic(userId, productId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("토글 실패: {}", e.getMessage());
                }
            });
        }

        // when
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeAsyncWithTasks(tasks);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        boolean isLiked = likeJpaRepository.existsByUserIdAndProductId(userId, productId);
        Long likeCount = likeJpaRepository.countByProductId(productId);
        
        log.info("=== 토글 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("토글 성공: {}, 실패: {}", successCount.get(), failCount.get());
        log.info("최종 좋아요 상태: {}", isLiked ? "좋아요" : "좋아요 없음");
        log.info("총 좋아요 수: {}", likeCount);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestLikeFacade testLikeFacade(LikeService likeService, 
                                            ProductCountService productCountService,
                                            TestProductCountService testProductCountService) {
            return new TestLikeFacade(likeService, productCountService, testProductCountService);
        }
        
        @Bean
        public TestProductCountService testProductCountService(ProductCountJpaRepository productCountJpaRepository) {
            return new TestProductCountService(productCountJpaRepository);
        }
    }

    // 테스트용 Facade - static 클래스로 선언 (인스턴스와 무관하게 동작)
    static class TestLikeFacade {
        private final LikeService likeService;
        private final ProductCountService productCountService;
        private final TestProductCountService testProductCountService;

        public TestLikeFacade(LikeService likeService, 
                            ProductCountService productCountService,
                            TestProductCountService testProductCountService) {
            this.likeService = likeService;
            this.productCountService = productCountService;
            this.testProductCountService = testProductCountService;
        }

        @Transactional
        public boolean addLikePessimistic(String userId, Long productId) {
            log.debug("addLikePessimistic 시작 - userId: {}, productId: {}", userId, productId);
            
            LikeCommand.Toggle command = new LikeCommand.Toggle(userId, productId);
            boolean added = likeService.addLike(command);
            log.debug("likeService.addLike 결과: {}", added);
            
            if (added) {
                // 테스트 전용 Service 계층을 통해 처리
                testProductCountService.incrementLikeCountWithPessimisticLock(productId);
                return true;
            }
            return false;
        }

        @Transactional
        @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
        )
        public boolean addLikeOptimistic(String userId, Long productId) {
            LikeCommand.Toggle command = new LikeCommand.Toggle(userId, productId);
            boolean added = likeService.addLike(command);
            
            if (added) {
                // 테스트 전용 Service 계층을 통해 처리
                testProductCountService.incrementLikeCountWithOptimisticLock(productId);
                return true;
            }
            return false;
        }

        @Transactional
        public boolean addLikeNoLock(String userId, Long productId) {
            LikeCommand.Toggle command = new LikeCommand.Toggle(userId, productId);
            boolean added = likeService.addLike(command);
            
            if (added) {
                // 테스트 전용 Service 계층을 통해 처리 (락 없이)
                testProductCountService.incrementLikeCountNoLock(productId);
                return true;
            }
            return false;
        }

        @Transactional
        public void toggleLikePessimistic(String userId, Long productId) {
            LikeCommand.IsLiked checkCommand = new LikeCommand.IsLiked(userId, productId);
            LikeCommand.Toggle toggleCommand = new LikeCommand.Toggle(userId, productId);
            
            if (likeService.isLiked(checkCommand)) {
                likeService.removeLike(toggleCommand);
            } else {
                likeService.addLike(toggleCommand);
            }
            productCountService.updateLikeCountPessimistic(
                new ProductCountCommand.UpdateLikeCount(productId)
            );
        }
    }
    
    // 테스트 전용 Service 계층 (Repository 접근을 캡슐화)
    @Service
    static class TestProductCountService {
        private final ProductCountJpaRepository productCountJpaRepository;
        
        public TestProductCountService(ProductCountJpaRepository productCountJpaRepository) {
            this.productCountJpaRepository = productCountJpaRepository;
        }
        
        @Transactional
        public void incrementLikeCountWithPessimisticLock(Long productId) {
            ProductCountEntity productCount = productCountJpaRepository
                .findByProductIdWithPessimisticLock(productId)
                .orElseGet(() -> new ProductCountEntity(productId));
            
            productCount.incrementLikeCount();
            productCountJpaRepository.save(productCount);
            log.debug("비관적 락 카운트 증가 완료 - 현재 값: {}", productCount.getLikeCount());
        }
        
        @Transactional
        public void incrementLikeCountWithOptimisticLock(Long productId) {
            ProductCountEntity productCount = productCountJpaRepository
                .findByProductIdWithOptimisticLock(productId)
                .orElseGet(() -> new ProductCountEntity(productId));
            
            productCount.incrementLikeCount();
            productCountJpaRepository.save(productCount);
        }
        
        @Transactional
        public void incrementLikeCountNoLock(Long productId) {
            ProductCountEntity productCount = productCountJpaRepository
                .findByProductId(productId)
                .orElseGet(() -> new ProductCountEntity(productId));
                
            productCount.incrementLikeCount();
            productCountJpaRepository.save(productCount);
        }
    }
}
