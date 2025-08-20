package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikedProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {
    
    private final LikeJpaRepository likeJpaRepository;
    
    @Override
    public LikeEntity save(LikeEntity like) {
        return likeJpaRepository.save(like);
    }
    
    @Override
    public void deleteByUserIdAndProductId(String userId, Long productId) {
        likeJpaRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    @Override
    public boolean existsByUserIdAndProductId(String userId, Long productId) {
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    @Override
    public Page<LikedProductDto> findLikedProductsByUserId(String userId, Pageable pageable) {
        return likeJpaRepository.findLikedProductsByUserId(userId, pageable);
    }
}
