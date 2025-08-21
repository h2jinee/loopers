package com.loopers.domain.stock;

import java.util.Optional;

public interface StockRepository {
    
    Optional<Stock> findByProductId(Long productId);
    
    Optional<Stock> findByProductIdWithLock(Long productId);
    
    Stock save(Stock stock);
}
