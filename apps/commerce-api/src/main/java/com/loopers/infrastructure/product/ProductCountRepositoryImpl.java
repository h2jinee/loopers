package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCount;
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
    public ProductCount save(ProductCount productCount) {
        return productCountJpaRepository.save(productCount);
    }
    
    @Override
    public Optional<ProductCount> findByProductId(Long productId) {
        return productCountJpaRepository.findByProductId(productId);
    }
    
    @Override
    public Optional<ProductCount> findByProductIdWithLock(Long productId) {
        return productCountJpaRepository.findByProductIdWithPessimisticLock(productId);
    }
    
    @Override
    public Long countLikesByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }
}
