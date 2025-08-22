package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
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
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
    
    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Optional<Order> findByIdAndUserId(Long orderId, String userId) {
        return orderJpaRepository.findByIdAndUserId(orderId, userId);
    }
    
    @Override
    public Page<Order> findByUserId(String userId, Pageable pageable) {
        return orderJpaRepository.findByUserId(userId, pageable);
    }
}
