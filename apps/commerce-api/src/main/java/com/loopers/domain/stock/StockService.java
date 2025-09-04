package com.loopers.domain.stock;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    
    private final StockRepository stockRepository;
    private final StockReservationRepository stockReservationRepository;
    
    /**
     * 단일 재고 정보 조회
     */
    public StockInfo getStockInfo(Long productId) {
        return stockRepository.findByProductId(productId)
            .map(this::toStockInfo)
            .orElse(new StockInfo(productId, 0, false));
    }
    
    /**
     * 여러 상품의 재고 정보 벌크 조회
     */
    public Map<Long, StockInfo> getStockInfosByProductIds(List<Long> productIds) {
        List<Stock> stocks = stockRepository.findByProductIdIn(productIds);
        
        Map<Long, StockInfo> stockMap = stocks.stream()
            .collect(Collectors.toMap(
                Stock::getProductId,
                this::toStockInfo
            ));
        
        productIds.forEach(productId -> 
            stockMap.computeIfAbsent(productId, 
                id -> new StockInfo(id, 0, false))
        );
        
        return stockMap;
    }
    
    /**
     * Stock Entity를 StockInfo로 변환
     */
    private StockInfo toStockInfo(Stock stock) {
        return new StockInfo(
            stock.getProductId(),
            stock.getQuantity(),
            stock.isAvailable(1)
        );
    }
    
    /**
     * 재고 차감 비즈니스 로직
     */
    public void processDecrease(Stock stock, Integer quantity) {
        stock.decrease(quantity);
    }
    
    /**
     * 재고 증가 비즈니스 로직
     */
    public void processIncrease(Stock stock, Integer quantity) {
        stock.increase(quantity);
    }
    
    /**
     * 구매 가능 여부 확인
     */
    public boolean isAvailable(Stock stock, Integer requestedQuantity) {
        if (stock == null) {
            return false;
        }
        return stock.isAvailable(requestedQuantity);
    }
    
    /**
     * 재고 예약 생성
     */
    @Transactional
    public void createReservation(Long orderId, Long productId, Integer quantity) {
        StockReservation reservation = new StockReservation(orderId, productId, quantity);
        stockReservationRepository.save(reservation);
    }
    
    /**
     * 재고 예약 확정 처리
     */
    public void confirmReservations(List<StockReservation> reservations) {
        reservations.forEach(StockReservation::confirm);
    }
    
    /**
     * 재고 예약 취소 처리
     */
    public void cancelReservations(List<StockReservation> reservations) {
        reservations.forEach(StockReservation::cancel);
    }
    
    /**
     * 재고 차감 (비관적 락 사용)
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "재고 정보를 찾을 수 없습니다. productId: " + productId));
        
        processDecrease(stock, quantity);
        stockRepository.save(stock);
        
        log.info("재고 차감 완료 - productId: {}, quantity: {}", productId, quantity);
    }
    
    
    /**
     * 재고 예약 확정
     */
    @Transactional
    public void confirmReservations(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        confirmReservations(reservations);
        stockReservationRepository.saveAll(reservations);
        
        log.info("재고 예약 확정 완료 - orderId: {}", orderId);
    }
    
    /**
     * 재고 롤백 (예약 취소 + 재고 복원)
     */
    @Transactional
    public void rollbackStock(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        
        cancelReservations(reservations);
        stockReservationRepository.saveAll(reservations);
        
        for (StockReservation reservation : reservations) {
            if (reservation.isCancelled()) {
                Stock stock = stockRepository.findByProductIdWithLock(reservation.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                        "재고 정보를 찾을 수 없습니다. productId: " + reservation.getProductId()));
                processIncrease(stock, reservation.getQuantity());
                stockRepository.save(stock);
            }
        }
        
        log.info("재고 롤백 완료 - orderId: {}", orderId);
    }
}
