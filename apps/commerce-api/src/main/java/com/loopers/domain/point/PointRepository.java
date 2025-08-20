package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    
    PointEntity save(PointEntity point);
    
    Optional<PointEntity> findByUserId(String userId);
    
    Optional<PointEntity> findByUserIdWithLock(String userId);
}
