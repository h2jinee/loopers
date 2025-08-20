package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductStockService {
    
    private final ProductStockRepository productStockRepository;
    
    @Transactional(readOnly = true)
    public boolean isAvailable(Long productId) {
        return productStockRepository.findByProductId(productId)
            .map(stock -> stock.getStock() > 0)
            .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public Integer getStock(Long productId) {
        return productStockRepository.findByProductId(productId)
            .map(ProductStockEntity::getStock)
            .orElse(0);
    }
    
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        ProductStockEntity stock = productStockRepository
            .findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "재고를 찾을 수 없습니다"));
        
        stock.decrease(quantity);
        productStockRepository.save(stock);
    }
    
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        ProductStockEntity stock = productStockRepository
            .findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "재고를 찾을 수 없습니다"));
        
        stock.increase(quantity);
        productStockRepository.save(stock);
    }
    
    @Transactional
    public void restoreStocks(Map<Long, Integer> stockUpdates) {
        stockUpdates.forEach((productId, quantity) -> {
            ProductStockEntity stock = productStockRepository
                .findByProductIdWithLock(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                    "재고를 찾을 수 없습니다. productId: " + productId));
            
            stock.increase(quantity);
            productStockRepository.save(stock);
        });
    }
}
