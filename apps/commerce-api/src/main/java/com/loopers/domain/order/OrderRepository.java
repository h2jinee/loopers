package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {
    
    Optional<OrderEntity> findByIdAndUserId(Long id, String userId);
    
    Page<OrderEntity> findByUserId(String userId, Pageable pageable);
}
