package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
        Page<ProductWithLikeCountDto> results = productJpaRepository.findAllWithLikeCountOptimized(pageable);
        
        List<ProductEntity> products = results.getContent().stream()
            .map(ProductWithLikeCountDto::product)
            .collect(Collectors.toList());
        
        return new PageImpl<>(products, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        Page<ProductWithLikeCountDto> results = productJpaRepository.findByBrandIdWithLikeCountOptimized(brandId, pageable);
        
        List<ProductEntity> products = results.getContent().stream()
            .map(ProductWithLikeCountDto::product)
            .collect(Collectors.toList());
        
        return new PageImpl<>(products, pageable, results.getTotalElements());
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
    public void incrementLikeCount(Long productId) {
        productJpaRepository.findById(productId).ifPresent(product -> {
            product.setLikeCount(product.getLikeCount() + 1);
            productJpaRepository.save(product);
        });
    }
    
    @Override
    public void decrementLikeCount(Long productId) {
        productJpaRepository.findById(productId).ifPresent(product -> {
            if (product.getLikeCount() > 0) {
                product.setLikeCount(product.getLikeCount() - 1);
                productJpaRepository.save(product);
            }
        });
    }
}
