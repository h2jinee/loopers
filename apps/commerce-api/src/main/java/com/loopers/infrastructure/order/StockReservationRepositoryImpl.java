package com.loopers.infrastructure.order;

import com.loopers.domain.order.StockReservationEntity;
import com.loopers.domain.order.StockReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class StockReservationRepositoryImpl implements StockReservationRepository {
    
    // TODO DB 추가 시 Map -> DB 변경해야 함.
    private final Map<Long, StockReservationEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public StockReservationEntity save(StockReservationEntity reservation) {
        if (reservation.getId() == null || reservation.getId() == 0L) {
            // 새 예약 생성
            try {
                var prePersistMethod = reservation.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(reservation);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist stock reservation", e);
            }
            
            Long newId = idGenerator.getAndIncrement();
            store.put(newId, reservation);
            return store.get(newId);
        } else {
            // 기존 예약 업데이트
            try {
                var preUpdateMethod = reservation.getClass().getSuperclass().getDeclaredMethod("preUpdate");
                preUpdateMethod.setAccessible(true);
                preUpdateMethod.invoke(reservation);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update stock reservation", e);
            }
            
            store.put(reservation.getId(), reservation);
            return reservation;
        }
    }

    @Override
    public Optional<StockReservationEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
    
    @Override
    public List<StockReservationEntity> findByOrderId(Long orderId) {
        return store.values().stream()
            .filter(reservation -> reservation.getOrderId().equals(orderId))
            .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
}
