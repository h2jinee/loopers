package com.loopers.domain.like;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeDomainService {
    
    private final LikeRepository likeRepository;
    private final ProductDomainService productDomainService;

    public boolean addLike(LikeCommand.Toggle command) {
        // 상품 존재 여부 확인
        ProductCommand.ValidateExists validateCommand = new ProductCommand.ValidateExists(command.productId());
        productDomainService.validateProductExists(validateCommand);
        
        // 이미 존재할 경우 false 반환
        if (likeRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            return false;
        }

		// 새로 추가된 경우 true 반환
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
