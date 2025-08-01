package com.loopers.domain.like;

import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeDomainService {
    
    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;

    public boolean addLike(LikeCommand.Toggle command) {
        if (!productRepository.existsById(command.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        
        if (likeRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            return false;
        }

        LikeEntity like = new LikeEntity(command.userId(), command.productId());
        likeRepository.save(like);
        return true;
    }
    
    public void removeLike(LikeCommand.Toggle command) {
        likeRepository.deleteByUserIdAndProductId(command.userId(), command.productId());
    }
    
    public boolean isLiked(LikeCommand.IsLiked command) {
        return likeRepository.existsByUserIdAndProductId(command.userId(), command.productId());
    }
}
