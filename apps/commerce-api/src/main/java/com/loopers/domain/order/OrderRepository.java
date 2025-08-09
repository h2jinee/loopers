package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {
    OrderEntity save(OrderEntity order);
    
    Optional<OrderEntity> findByIdAndUserId(Long orderId, String userId);
    
    Page<OrderEntity> findByUserId(String userId, Pageable pageable);
}
