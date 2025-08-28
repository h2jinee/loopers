package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistory;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    
    private final PointHistoryJpaRepository pointHistoryJpaRepository;
    
    @Override
    public PointHistory save(PointHistory history) {
        return pointHistoryJpaRepository.save(history);
    }
}
