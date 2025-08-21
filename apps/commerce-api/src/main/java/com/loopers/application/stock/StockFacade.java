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
    private final StockReservationRepository stockReservationRepository;
    private final StockService stockService;
    
    /**
     * 재고 조회
     */
    @Transactional(readOnly = true)
    public Integer getStock(Long productId) {
        return stockRepository.findByProductId(productId)
            .map(Stock::getQuantity)
            .orElse(0);
    }
    
    /**
     * 재고 사용 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isAvailable(Long productId, Integer requestedQuantity) {
        return stockRepository.findByProductId(productId)
            .map(stock -> stock.isAvailable(requestedQuantity))
            .orElse(false);
    }
    
    /**
     * 재고 사용 가능 여부 확인 (수량 1개)
     */
    @Transactional(readOnly = true)
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
    
    /**
     * 재고 증가
     */
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "재고 정보를 찾을 수 없습니다. productId: " + productId));
        
        stockService.processIncrease(stock, quantity);
        stockRepository.save(stock);
        
        log.info("재고 증가 완료 - productId: {}, quantity: {}", productId, quantity);
    }
    
    /**
     * 재고 복원 (여러 상품 한번에)
     */
    @Transactional
    public void restoreStocks(Map<Long, Integer> stockUpdates) {
        stockUpdates.forEach((productId, quantity) -> {
            Stock stock = stockRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                    "재고 정보를 찾을 수 없습니다. productId: " + productId));
            
            stockService.processIncrease(stock, quantity);
            stockRepository.save(stock);
        });
        
        log.info("재고 복원 완료 - {} 개 상품", stockUpdates.size());
    }
    
    /**
     * 재고 초기화 (상품 등록 시)
     */
    @Transactional
    public void initializeStock(Long productId, Integer initialQuantity) {
        // 이미 재고가 있는지 확인
        if (stockRepository.findByProductId(productId).isPresent()) {
            log.info("이미 재고가 존재합니다. productId: {}", productId);
            return;
        }
        
        Stock stock = stockService.createNewStock(productId, initialQuantity);
        stockRepository.save(stock);
        
        log.info("재고 초기화 완료 - productId: {}, initialQuantity: {}", productId, initialQuantity);
    }
    
    /**
     * 재고 조정 (재고 실사 등)
     */
    @Transactional
    public void adjustStock(Long productId, Integer newQuantity) {
        Stock stock = stockRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "재고 정보를 찾을 수 없습니다. productId: " + productId));
        
        stockService.processAdjustment(stock, newQuantity);
        stockRepository.save(stock);
        
        log.info("재고 조정 완료 - productId: {}, newQuantity: {}", productId, newQuantity);
    }
    
    /**
     * 재고 예약 생성
     */
    @Transactional
    public StockReservation createReservation(Long orderId, Long productId, Integer quantity) {
        StockReservation reservation = stockService.createReservation(orderId, productId, quantity);
        return stockReservationRepository.save(reservation);
    }
    
    /**
     * 재고 예약 확정
     */
    @Transactional
    public void confirmReservations(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        stockService.confirmReservations(reservations);
        stockReservationRepository.saveAll(reservations);
        
        log.info("재고 예약 확정 완료 - orderId: {}", orderId);
    }
    
    /**
     * 재고 예약 취소 및 재고 복원
     */
    @Transactional
    public void cancelReservationsAndRestoreStock(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        
        // 예약 취소
        stockService.cancelReservations(reservations);
        stockReservationRepository.saveAll(reservations);
        
        // 재고 복원
        for (StockReservation reservation : reservations) {
            if (reservation.isCancelled()) {
                Stock stock = stockRepository.findByProductIdWithLock(reservation.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                        "재고 정보를 찾을 수 없습니다. productId: " + reservation.getProductId()));
                stockService.processIncrease(stock, reservation.getQuantity());
                stockRepository.save(stock);
            }
        }
        
        log.info("재고 예약 취소 및 복원 완료 - orderId: {}", orderId);
    }
}
