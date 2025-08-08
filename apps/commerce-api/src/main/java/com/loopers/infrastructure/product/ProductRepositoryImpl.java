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
    private final ProductCountJpaRepository productCountRepository;

    @Override
    public Page<ProductEntity> findAllWithLikeCount(Pageable pageable) {
        Page<ProductEntity> products = productJpaRepository.findAll(pageable);
        
        if (products.isEmpty()) {
            return products;
        }
        
        List<Long> productIds = products.getContent().stream()
            .map(ProductEntity::getId)
            .collect(Collectors.toList());
        
        Map<Long, Long> likeCountMap = productCountRepository.findByProductIdIn(productIds)
            .stream()
            .collect(Collectors.toMap(
                ProductCountEntity::getProductId,
                ProductCountEntity::getLikeCount,
                (existing, replacement) -> existing
            ));
        
        products.getContent().forEach(product -> 
            product.setLikeCount(likeCountMap.getOrDefault(product.getId(), 0L))
        );
        
        return products;
    }
    
    @Override
    public Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        Page<ProductEntity> products = productJpaRepository.findByBrandId(brandId, pageable);
        
        if (products.isEmpty()) {
            return products;
        }

        List<Long> productIds = products.getContent().stream()
            .map(ProductEntity::getId)
            .collect(Collectors.toList());
        
        Map<Long, Long> likeCountMap = productCountRepository.findByProductIdIn(productIds)
            .stream()
            .collect(Collectors.toMap(
                ProductCountEntity::getProductId,
                ProductCountEntity::getLikeCount,
                (existing, replacement) -> existing
            ));
        
        products.getContent().forEach(product -> 
            product.setLikeCount(likeCountMap.getOrDefault(product.getId(), 0L))
        );
        
        return products;
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
}
