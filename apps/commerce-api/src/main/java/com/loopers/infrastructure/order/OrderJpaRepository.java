package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByIdAndUserId(Long id, String userId);
    
    Page<OrderEntity> findByUserId(String userId, Pageable pageable);
}
