package com.loopers.domain.product;

import com.loopers.infrastructure.product.ProductStockJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductStockService {
    
    private final ProductStockJpaRepository productStockJpaRepository;
    
    @Transactional(readOnly = true)
    public boolean isAvailable(Long productId) {
        return productStockJpaRepository.findByProductId(productId)
            .map(stock -> stock.getStock() > 0)
            .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public boolean hasStock(Long productId, Integer quantity) {
        return productStockJpaRepository.findByProductId(productId)
            .map(stock -> stock.getStock() >= quantity)
            .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public Integer getStock(Long productId) {
        return productStockJpaRepository.findByProductId(productId)
            .map(ProductStockEntity::getStock)
            .orElse(0);
    }
    
    public void decreaseStock(Long productId, Integer quantity) {
        ProductStockEntity stock = productStockJpaRepository
            .findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "재고를 찾을 수 없습니다"));
        
        stock.decrease(quantity);
        productStockJpaRepository.save(stock);
    }
    
    public void increaseStock(Long productId, Integer quantity) {
        ProductStockEntity stock = productStockJpaRepository
            .findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "재고를 찾을 수 없습니다"));
        
        stock.increase(quantity);
        productStockJpaRepository.save(stock);
    }
}
