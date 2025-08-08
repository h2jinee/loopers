package com.loopers.domain.like;

import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    
    private final LikeJpaRepository likeJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    public boolean addLike(LikeCommand.Toggle command) {
        if (!productJpaRepository.existsById(command.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        
        if (likeJpaRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            return false;
        }

        LikeEntity like = new LikeEntity(command.userId(), command.productId());
        likeJpaRepository.save(like);
        return true;
    }
    
    public void removeLike(LikeCommand.Toggle command) {
        likeJpaRepository.deleteByUserIdAndProductId(command.userId(), command.productId());
    }
    
    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeJpaRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }
}
