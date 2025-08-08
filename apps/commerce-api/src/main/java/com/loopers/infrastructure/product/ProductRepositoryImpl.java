package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.product.ProductCountJpaRepository;
import com.loopers.domain.product.ProductCountEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductCountJpaRepository productCountRepository;

    @Override
    public Page<ProductEntity> findAllWithLikeCount(Pageable pageable) {
        // 1. 상품 목록 조회
        Page<ProductEntity> products = productJpaRepository.findAll(pageable);
        
        // 빈 페이지면 그대로 반환
        if (products.isEmpty()) {
            return products;
        }
        
        // 2. 상품 ID 목록 추출
        List<Long> productIds = products.getContent().stream()
            .map(ProductEntity::getId)
            .collect(Collectors.toList());
        
        // 3. 좋아요 수 조회
        Map<Long, Long> likeCountMap = productCountRepository.findByProductIdIn(productIds)
            .stream()
            .collect(Collectors.toMap(
                ProductCountEntity::getProductId,
                ProductCountEntity::getLikeCount,
                (existing, replacement) -> existing  // 중복 키 처리
            ));
        
        // 4. 메모리에서 좋아요 수 매핑
        products.getContent().forEach(product -> 
            product.setLikeCount(likeCountMap.getOrDefault(product.getId(), 0L))
        );
        
        return products;
    }
    
    @Override
    public Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        // 1. 브랜드별 상품 목록 조회
        Page<ProductEntity> products = productJpaRepository.findByBrandId(brandId, pageable);
        
        // 빈 페이지면 그대로 반환
        if (products.isEmpty()) {
            return products;
        }

        // 2. 상품 ID 목록 추출
        List<Long> productIds = products.getContent().stream()
            .map(ProductEntity::getId)
            .collect(Collectors.toList());
        
        // 3. 좋아요 수 조회
        Map<Long, Long> likeCountMap = productCountRepository.findByProductIdIn(productIds)
            .stream()
            .collect(Collectors.toMap(
                ProductCountEntity::getProductId,
                ProductCountEntity::getLikeCount,
                (existing, replacement) -> existing  // 중복 키 처리
            ));
        
        // 4. 메모리에서 좋아요 수 매핑
        products.getContent().forEach(product -> 
            product.setLikeCount(likeCountMap.getOrDefault(product.getId(), 0L))
        );
        
        return products;
    }
}
