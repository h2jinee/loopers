package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryEntity;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    
    private final PointHistoryJpaRepository pointHistoryJpaRepository;
    
    @Override
    public PointHistoryEntity save(PointHistoryEntity history) {
        return pointHistoryJpaRepository.save(history);
    }
}
