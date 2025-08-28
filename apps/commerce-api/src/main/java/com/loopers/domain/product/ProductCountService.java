package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCountService {
    
    private final ProductCountRepository productCountRepository;
    private final ProductRepository productRepository;

    /**
     * 비관적 락을 사용한 좋아요 카운트 업데이트
     * 캐시 무효화 포함
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "#command.productId()"),
        @CacheEvict(value = "productList", allEntries = true)
    })
    public void updateLikeCountPessimistic(ProductCountCommand.UpdateLikeCount command) {
        // 1. 락 획득
        ProductCount productCount = productCountRepository.findByProductIdWithLock(command.productId())
            .orElseGet(() -> new ProductCount(command.productId()));
        
        // 2. 락 획득 후 COUNT 쿼리 실행
        Long likeCount = productCountRepository.countLikesByProductId(command.productId());
        
        // 3. 업데이트
        productCount.updateLikeCount(likeCount);
        productCountRepository.save(productCount);
    }
    
    /**
     * 좋아요 카운트 증가 (비관적 락)
     * 캐시 무효화 포함
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "#productId"),
        @CacheEvict(value = "productList", allEntries = true)
    })
    public Long incrementLikeCountWithLock(Long productId) {
        ProductCount productCount = productCountRepository
            .findByProductIdWithLock(productId)
            .orElseGet(() -> new ProductCount(productId));
        
        productCount.incrementLikeCount();
        productCountRepository.save(productCount);
        
        // products 테이블도 동기화 (비정규화)
        productRepository.incrementLikeCount(productId);
        
        return productCount.getLikeCount();
    }
    
    /**
     * 좋아요 카운트 감소 (비관적 락)
     * 음수 방지 로직은 ProductCount 내부에서 처리
     * 캐시 무효화 포함
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "#productId"),
        @CacheEvict(value = "productList", allEntries = true)
    })
    public Long decrementLikeCountWithLock(Long productId) {
        ProductCount productCount = productCountRepository
            .findByProductIdWithLock(productId)
            .orElseThrow(() -> new IllegalStateException("상품 카운트 정보가 없습니다."));
        
        productCount.decrementLikeCount();
        productCountRepository.save(productCount);
        
        // products 테이블도 동기화 (비정규화)
        productRepository.decrementLikeCount(productId);
        
        return productCount.getLikeCount();
    }
    
    /**
     * 좋아요 카운트 조회
     */
    public Long getLikeCount(Long productId) {
        return productCountRepository.findByProductId(productId)
            .map(ProductCount::getLikeCount)
            .orElse(0L);
    }
}
