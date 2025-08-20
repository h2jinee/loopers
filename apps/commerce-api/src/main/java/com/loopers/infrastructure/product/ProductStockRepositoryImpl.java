package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductStockEntity;
import com.loopers.domain.product.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductStockRepositoryImpl implements ProductStockRepository {
    
    private final ProductStockJpaRepository productStockJpaRepository;
    
    @Override
    public Optional<ProductStockEntity> findByProductId(Long productId) {
        return productStockJpaRepository.findByProductId(productId);
    }
    
    @Override
    public Optional<ProductStockEntity> findByProductIdWithLock(Long productId) {
        return productStockJpaRepository.findByProductIdWithPessimisticLock(productId);
    }
    
    @Override
    public ProductStockEntity save(ProductStockEntity stock) {
        return productStockJpaRepository.save(stock);
    }
}
