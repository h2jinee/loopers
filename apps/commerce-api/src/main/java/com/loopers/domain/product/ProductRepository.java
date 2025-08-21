package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    Optional<Product> findById(Long productId);
    
    Page<Product> findAllWithLikeCount(Pageable pageable);
    
    Page<Product> findByBrandIdWithLikeCount(Long brandId, Pageable pageable);
    
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(Long brandId, Pageable pageable);
    
    List<ProductStockInfo> findProductStockInfoByIds(List<Long> productIds);
    
    Product save(Product product);
    
    void incrementLikeCount(Long productId);
    
    void decrementLikeCount(Long productId);
}
