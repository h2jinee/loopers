package com.loopers.domain.stock;

import java.util.List;

public interface StockReservationRepository {
    
    StockReservation save(StockReservation reservation);
    
    List<StockReservation> saveAll(List<StockReservation> reservations);
    
    List<StockReservation> findByOrderId(Long orderId);
    
    List<StockReservation> findByProductId(Long productId);
}
