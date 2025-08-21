package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationJpaRepository extends JpaRepository<StockReservation, Long> {
    
    List<StockReservation> findByOrderId(Long orderId);
    
    List<StockReservation> findByProductId(Long productId);
}
