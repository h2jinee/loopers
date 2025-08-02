package com.loopers.domain.order;

import java.util.List;
import java.util.Optional;

public interface StockReservationRepository {
    StockReservationEntity save(StockReservationEntity reservation);
    
    Optional<StockReservationEntity> findById(Long id);
    
    List<StockReservationEntity> findByOrderId(Long orderId);

    void clear();
}
