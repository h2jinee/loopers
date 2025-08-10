package com.loopers.domain.like;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepository {
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    Long countByProductId(Long productId);
    
    Page<LikedProductDto> findLikedProductsByUserId(String userId, Pageable pageable);
}
