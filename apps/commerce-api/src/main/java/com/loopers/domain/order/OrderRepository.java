package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    
    Optional<Order> findById(Long orderId);
    
    Optional<Order> findByIdAndUserId(Long orderId, String userId);
    
    Page<Order> findByUserId(String userId, Pageable pageable);
}
