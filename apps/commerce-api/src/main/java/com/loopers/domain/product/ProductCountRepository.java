package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductCountRepository {
    
    Optional<ProductCountEntity> findByProductId(Long productId);
    
    List<ProductCountEntity> findByProductIdIn(List<Long> productIds);
}
