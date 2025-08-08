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

    // TODO 배치로 변경 필요
    @Transactional
    public void updateLikeCount(ProductCountCommand.UpdateLikeCount command) {
        Long likeCount = likeJpaRepository.countByProductId(command.productId());
        
        ProductCountEntity productCount = productCountJpaRepository.findByProductId(command.productId())
            .orElseGet(() -> new ProductCountEntity(command.productId()));
        
        productCount.updateLikeCount(likeCount);
        productCountJpaRepository.save(productCount);
    }
    
    public Long getLikeCount(ProductCountCommand.GetLikeCount command) {
        return productCountJpaRepository.findByProductId(command.productId())
            .map(ProductCountEntity::getLikeCount)
            .orElse(0L);
    }
}
