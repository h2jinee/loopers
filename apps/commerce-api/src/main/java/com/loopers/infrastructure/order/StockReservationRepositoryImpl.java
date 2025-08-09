package com.loopers.infrastructure.order;

import com.loopers.domain.order.StockReservationEntity;
import com.loopers.domain.order.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockReservationRepositoryImpl implements StockReservationRepository {
    
    private final StockReservationJpaRepository stockReservationJpaRepository;
    
    @Override
    public StockReservationEntity save(StockReservationEntity reservation) {
        return stockReservationJpaRepository.save(reservation);
    }
    
    @Override
    public List<StockReservationEntity> saveAll(List<StockReservationEntity> reservations) {
        return stockReservationJpaRepository.saveAll(reservations);
    }
    
    @Override
    public List<StockReservationEntity> findByOrderId(Long orderId) {
        return stockReservationJpaRepository.findByOrderId(orderId);
    }
}
