package com.loopers.domain.product;

import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCountService {
    
    private final ProductCountRepository productCountRepository;
    private final LikeRepository likeRepository;

    // TODO 배치로 변경 필요
    @Transactional
    public void updateLikeCount(ProductCountCommand.UpdateLikeCount command) {
        Long likeCount = likeRepository.countByProductId(command.productId());
        
        ProductCountEntity productCount = productCountRepository.findByProductId(command.productId())
            .orElseGet(() -> new ProductCountEntity(command.productId()));
        
        productCount.updateLikeCount(likeCount);
        productCountRepository.save(productCount);
    }
    
    public Long getLikeCount(ProductCountCommand.GetLikeCount command) {
        return productCountRepository.findByProductId(command.productId())
            .map(ProductCountEntity::getLikeCount)
            .orElse(0L);
    }
}
