package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCountEntity;
import com.loopers.domain.product.ProductCountRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductCountRepositoryImpl implements ProductCountRepository {
    
    private final ProductCountJpaRepository productCountJpaRepository;
    private final LikeJpaRepository likeJpaRepository;
    
    @Override
    public ProductCountEntity save(ProductCountEntity productCount) {
        return productCountJpaRepository.save(productCount);
    }
    
    @Override
    public Optional<ProductCountEntity> findByProductId(Long productId) {
        return productCountJpaRepository.findByProductId(productId);
    }
    
    @Override
    public Optional<ProductCountEntity> findByProductIdWithPessimisticLock(Long productId) {
        return productCountJpaRepository.findByProductIdWithPessimisticLock(productId);
    }
    
    @Override
    public Long countLikesByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }
}
