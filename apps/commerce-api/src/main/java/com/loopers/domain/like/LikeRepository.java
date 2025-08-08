package com.loopers.domain.like;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepository {
    
    Page<LikeEntity> findByUserId(String userId, Pageable pageable);
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    Long countByProductId(Long productId);
}
