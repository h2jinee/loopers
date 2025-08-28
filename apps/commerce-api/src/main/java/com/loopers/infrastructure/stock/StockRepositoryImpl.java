package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {
    
    private final StockJpaRepository stockJpaRepository;
    
    @Override
    public Optional<Stock> findByProductId(Long productId) {
        return stockJpaRepository.findByProductId(productId);
    }
    
    @Override
    public Optional<Stock> findByProductIdWithLock(Long productId) {
        return stockJpaRepository.findByProductIdWithPessimisticLock(productId);
    }
    
    @Override
    public List<Stock> findByProductIdIn(List<Long> productIds) {
        return stockJpaRepository.findByProductIdIn(productIds);
    }
    
    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(stock);
    }
}
