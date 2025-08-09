package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCountService {
    
    private final ProductCountRepository productCountRepository;

	// 비관적 락
    @Transactional
    public void updateLikeCountPessimistic(ProductCountCommand.UpdateLikeCount command) {
        // 1. 락 획득
        ProductCountEntity productCount = productCountRepository.findByProductIdWithPessimisticLock(command.productId())
            .orElseGet(() -> new ProductCountEntity(command.productId()));
        
        // 2. 락 획득 후 COUNT 쿼리 실행
        Long likeCount = productCountRepository.countLikesByProductId(command.productId());
        
        // 3. 업데이트
        productCount.updateLikeCount(likeCount);
        productCountRepository.save(productCount);
    }
    
    /**
     * 좋아요 카운트 증가 (비관적 락)
     */
    @Transactional
    public Long incrementLikeCountWithLock(Long productId) {
        ProductCountEntity productCount = productCountRepository
            .findByProductIdWithPessimisticLock(productId)
            .orElseGet(() -> new ProductCountEntity(productId));
        
        productCount.incrementLikeCount();
        productCountRepository.save(productCount);
        
        return productCount.getLikeCount();
    }
    
    /**
     * 좋아요 카운트 감소 (비관적 락)
     * 음수 방지 로직은 ProductCountEntity 내부에서 처리
     */
    @Transactional
    public Long decrementLikeCountWithLock(Long productId) {
        ProductCountEntity productCount = productCountRepository
            .findByProductIdWithPessimisticLock(productId)
            .orElseThrow(() -> new IllegalStateException("상품 카운트 정보가 없습니다."));
        
        productCount.decrementLikeCount();
        productCountRepository.save(productCount);
        
        return productCount.getLikeCount();
    }
    
    /**
     * 좋아요 카운트 조회
     */
    public Long getLikeCount(Long productId) {
        return productCountRepository.findByProductId(productId)
            .map(ProductCountEntity::getLikeCount)
            .orElse(0L);
    }
}
