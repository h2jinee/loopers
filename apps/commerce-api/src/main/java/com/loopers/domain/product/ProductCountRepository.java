package com.loopers.domain.product;

import java.util.Optional;

public interface ProductCountRepository {
    ProductCount save(ProductCount productCount);
    
    Optional<ProductCount> findByProductId(Long productId);
    
    Optional<ProductCount> findByProductIdWithLock(Long productId);
    
    Long countLikesByProductId(Long productId);
}
