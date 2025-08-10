package com.loopers.domain.order;

import java.util.List;

public interface StockReservationRepository {
    StockReservationEntity save(StockReservationEntity reservation);
    
    List<StockReservationEntity> saveAll(List<StockReservationEntity> reservations);
    
    List<StockReservationEntity> findByOrderId(Long orderId);
}
