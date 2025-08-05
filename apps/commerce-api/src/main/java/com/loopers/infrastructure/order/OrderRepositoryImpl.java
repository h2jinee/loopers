package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    
    // TODO DB 추가 시 Map -> DB 변경해야 함.
    private final Map<Long, OrderEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public OrderEntity save(OrderEntity order) {
        if (order.getId() == null || order.getId() == 0L) {
            // 새 주문 생성
            try {
                var prePersistMethod = order.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(order);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist order", e);
            }
            
            Long newId = idGenerator.getAndIncrement();
            store.put(newId, order);
            return store.get(newId);
        } else {
            // 기존 주문 업데이트
            try {
                var preUpdateMethod = order.getClass().getSuperclass().getDeclaredMethod("preUpdate");
                preUpdateMethod.setAccessible(true);
                preUpdateMethod.invoke(order);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update order", e);
            }
            
            store.put(order.getId(), order);
            return order;
        }
    }
    
    @Override
    public Optional<OrderEntity> findById(Long orderId) {
        return Optional.ofNullable(store.get(orderId));
    }
    
    @Override
    public Optional<OrderEntity> findByIdAndUserId(Long orderId, String userId) {
        return findById(orderId)
            .filter(order -> order.getUserId().equals(userId));
    }
    
    @Override
    public Page<OrderEntity> findByUserId(String userId, Pageable pageable) {
        List<OrderEntity> userOrders = store.values().stream()
            .filter(order -> order.getUserId().equals(userId))
            .sorted(getComparator(pageable.getSort()))
            .collect(Collectors.toList());
        
        return createPage(userOrders, pageable);
    }
    
    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
    
    private Page<OrderEntity> createPage(List<OrderEntity> orders, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), orders.size());
        
        if (start >= orders.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, orders.size());
        }
        
        List<OrderEntity> pageContent = orders.subList(start, end);
        return new PageImpl<>(pageContent, pageable, orders.size());
    }
    
    private Comparator<OrderEntity> getComparator(Sort sort) {
        if (sort.isEmpty()) {
            return Comparator.comparing(OrderEntity::getCreatedAt).reversed();
        }
        
        Sort.Order order = sort.iterator().next();
        String property = order.getProperty();
        
        Comparator<OrderEntity> comparator = switch (property) {
            case "createdAt" -> Comparator.comparing(OrderEntity::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "totalAmount" -> Comparator.comparing(o -> o.getTotalAmount().amount());
            default -> Comparator.comparing(OrderEntity::getCreatedAt);
        };
        
        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
