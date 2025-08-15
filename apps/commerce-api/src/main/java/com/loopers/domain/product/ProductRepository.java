package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    Optional<ProductEntity> findById(Long productId);
    
    List<ProductEntity> findAllByIdIn(List<Long> productIds);
    
    Page<ProductEntity> findAllWithLikeCount(Pageable pageable);
    
    Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable);
    
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(Long brandId, Pageable pageable);
    
    List<ProductStockInfo> findProductStockInfoByIds(List<Long> productIds);
    
    ProductEntity save(ProductEntity product);
    
    void incrementLikeCount(Long productId);
    
    void decrementLikeCount(Long productId);
}
