package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long>, OrderRepository {

}
