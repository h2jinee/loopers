package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductRepository {
    
    Page<ProductEntity> findAllWithLikeCount(Pageable pageable);
    
    Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable);
    
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(Long brandId, Pageable pageable);
    
    List<ProductStockInfo> findProductStockInfoByIds(List<Long> productIds);
}
