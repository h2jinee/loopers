package com.loopers.domain.like;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepository {
    
    Like save(Like like);
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    Page<LikedProductDto> findByUserId(String userId, Pageable pageable);
}
