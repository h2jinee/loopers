package com.loopers.domain.point;

public interface PointHistoryRepository {
    PointHistoryEntity save(PointHistoryEntity history);
    
    void clear();
}
