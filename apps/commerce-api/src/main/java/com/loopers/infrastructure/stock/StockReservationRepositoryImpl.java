package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.StockReservation;
import com.loopers.domain.stock.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockReservationRepositoryImpl implements StockReservationRepository {
    
    private final StockReservationJpaRepository stockReservationJpaRepository;
    
    @Override
    public StockReservation save(StockReservation reservation) {
        return stockReservationJpaRepository.save(reservation);
    }
    
    @Override
    public List<StockReservation> saveAll(List<StockReservation> reservations) {
        return stockReservationJpaRepository.saveAll(reservations);
    }
    
    @Override
    public List<StockReservation> findByOrderId(Long orderId) {
        return stockReservationJpaRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<StockReservation> findByProductId(Long productId) {
        return stockReservationJpaRepository.findByProductId(productId);
    }
}
