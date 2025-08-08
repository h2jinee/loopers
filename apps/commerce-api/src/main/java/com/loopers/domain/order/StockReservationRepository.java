package com.loopers.domain.order;

import java.util.List;

public interface StockReservationRepository {

    List<StockReservationEntity> findByOrderId(Long orderId);
}
