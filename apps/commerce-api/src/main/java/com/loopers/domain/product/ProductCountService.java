package com.loopers.domain.product;

import com.loopers.infrastructure.product.ProductCountJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCountService {
    
    private final ProductCountJpaRepository productCountJpaRepository;
    private final LikeJpaRepository likeJpaRepository;

	// 비관적 락
    @Transactional
    public void updateLikeCountPessimistic(ProductCountCommand.UpdateLikeCount command) {
        // 1. 락 획득
        ProductCountEntity productCount = productCountJpaRepository.findByProductIdWithPessimisticLock(command.productId())
            .orElseGet(() -> new ProductCountEntity(command.productId()));
        
        // 2. 락 획득 후 COUNT 쿼리 실행
        Long likeCount = likeJpaRepository.countByProductId(command.productId());
        
        // 3. 업데이트
        productCount.updateLikeCount(likeCount);
        productCountJpaRepository.save(productCount);
    }
}
