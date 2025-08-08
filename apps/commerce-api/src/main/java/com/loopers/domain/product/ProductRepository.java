package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    
    Page<ProductEntity> findAllWithLikeCount(Pageable pageable);
    
    Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable);
}
