package com.loopers.domain.product;

import java.util.Optional;

public interface ProductCountRepository {
    ProductCountEntity save(ProductCountEntity productCount);
    
    Optional<ProductCountEntity> findByProductId(Long productId);
    
    void clear();
}
