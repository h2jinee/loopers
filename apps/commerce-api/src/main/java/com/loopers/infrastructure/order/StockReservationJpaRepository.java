package com.loopers.infrastructure.order;

import com.loopers.domain.order.StockReservationEntity;
import com.loopers.domain.order.StockReservationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long>, StockReservationRepository {

}
