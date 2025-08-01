package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    ProductEntity save(ProductEntity product);
    
    Optional<ProductEntity> findById(Long productId);

    Page<ProductEntity> findAllWithLikeCount(Pageable pageable);
    
    Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable);
    
    boolean existsById(Long productId);
    
    void clear();
}
