package com.loopers.infrastructure.order;

import com.loopers.domain.order.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {
    
    List<StockReservationEntity> findByOrderId(Long orderId);
}
