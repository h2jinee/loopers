package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    
    @Override
    public Optional<ProductEntity> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }
    
    @Override
    public List<ProductEntity> findAllByIdIn(List<Long> productIds) {
        return productJpaRepository.findAllByIdIn(productIds);
    }

    @Override
    public Page<ProductEntity> findAllWithLikeCount(Pageable pageable) {
        return productJpaRepository.findAllByOrderByLikeCountDesc(pageable);
    }
    
    @Override
    public Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        return productJpaRepository.findByBrandIdOrderByLikeCountDesc(brandId, pageable);
    }
    
    @Override
    public Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable) {
        return productJpaRepository.findAllProductsWithBrand(pageable);
    }
    
    @Override
    public Page<ProductWithBrandDto> findProductsWithBrandByBrandId(Long brandId, Pageable pageable) {
        return productJpaRepository.findProductsWithBrandByBrandId(brandId, pageable);
    }
    
    @Override
    public List<ProductStockInfo> findProductStockInfoByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productJpaRepository.findProductStockInfoByIds(productIds);
    }

    @Override
    public ProductEntity save(ProductEntity product) {
        return productJpaRepository.save(product);
    }
    
    @Override
    @Transactional
    public void incrementLikeCount(Long productId) {
        productJpaRepository.incrementLikeCount(productId);
    }
    
    @Override
    @Transactional
    public void decrementLikeCount(Long productId) {
        productJpaRepository.decrementLikeCount(productId);
    }
}
