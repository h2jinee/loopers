package com.loopers.application.stock;

import com.loopers.domain.stock.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockFacade {
    
    private final StockRepository stockRepository;
    private final StockService stockService;
    
    /**
     * 재고 사용 가능 여부 확인
     */
    public boolean isAvailable(Long productId, Integer requestedQuantity) {
        return stockRepository.findByProductId(productId)
            .map(stock -> stock.isAvailable(requestedQuantity))
            .orElse(false);
    }
    
    /**
     * 재고 사용 가능 여부 확인 (수량 1개)
     */
    public boolean isAvailable(Long productId) {
        return isAvailable(productId, 1);
    }
    
    /**
     * 재고 차감
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "재고 정보를 찾을 수 없습니다. productId: " + productId));
        
        stockService.processDecrease(stock, quantity);
        stockRepository.save(stock);
        
        log.info("재고 차감 완료 - productId: {}, quantity: {}", productId, quantity);
    }
}
