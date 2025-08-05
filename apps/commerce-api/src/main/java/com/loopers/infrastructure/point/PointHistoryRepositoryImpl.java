package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryEntity;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    
    // TODO DB 추가 시 Map -> DB 변경해야 함.
    private final Map<Long, PointHistoryEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public PointHistoryEntity save(PointHistoryEntity history) {
        if (history.getId() == null || history.getId() == 0L) {
            // 새 히스토리 생성 시 prePersist 호출
            try {
                var prePersistMethod = history.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(history);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist point history", e);
            }
            
            // ID 생성
            Long newId = idGenerator.getAndIncrement();
            store.put(newId, history);
            
            // 생성된 ID로 새 엔티티 반환
            return store.get(newId);
        } else {
            store.put(history.getId(), history);
            return history;
        }
    }

    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
}
