package com.loopers.domain.stock;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {
    
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
     * 재고 조정 비즈니스 로직
     */
    public void processAdjustment(Stock stock, Integer newQuantity) {
        stock.adjust(newQuantity);
    }
    
    /**
     * 재고 생성
     */
    public Stock createNewStock(Long productId, Integer initialQuantity) {
        return new Stock(productId, initialQuantity);
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
    public StockReservation createReservation(Long orderId, Long productId, Integer quantity) {
        return new StockReservation(orderId, productId, quantity);
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
     * 예약된 총 수량 계산 (RESERVED 상태만)
     */
    public Integer calculateReservedQuantity(List<StockReservation> reservations) {
        return reservations.stream()
            .filter(StockReservation::isReserved)
            .mapToInt(StockReservation::getQuantity)
            .sum();
    }
    
    /**
     * 실제 가용 재고 계산 (전체 재고 - 예약된 재고)
     */
    public Integer calculateAvailableStock(Integer totalStock, Integer reservedStock) {
        return totalStock - reservedStock;
    }
}
