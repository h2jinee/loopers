package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderJpaRepository orderJpaRepository;
    
    @Override
    public OrderEntity save(OrderEntity order) {
        return orderJpaRepository.save(order);
    }
    
    @Override
    public Optional<OrderEntity> findByIdAndUserId(Long orderId, String userId) {
        return orderJpaRepository.findByIdAndUserId(orderId, userId);
    }
    
    @Override
    public Page<OrderEntity> findByUserId(String userId, Pageable pageable) {
        return orderJpaRepository.findByUserId(userId, pageable);
    }
}
