package com.loopers.domain.stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    
    Optional<Stock> findByProductId(Long productId);
    
    Optional<Stock> findByProductIdWithLock(Long productId);
    
    List<Stock> findByProductIdIn(List<Long> productIds);
    
    Stock save(Stock stock);
}
