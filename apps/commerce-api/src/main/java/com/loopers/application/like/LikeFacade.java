package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikedProductDto;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductCountEntity;
import com.loopers.infrastructure.product.ProductCountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeService likeService;
    private final ProductCountJpaRepository productCountRepository;

    /**
     * 좋아요 추가
     */
    @Transactional
    public LikeResult.LikeToggleResult addLike(LikeCriteria.AddLike criteria) {
        log.debug("좋아요 추가 시작 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
        
        // 1. 좋아요 중복 체크 및 추가
        LikeCommand.Toggle command = criteria.toCommand();
        boolean added = likeService.addLike(command);
        
        if (added) {
            // 2. 카운트 증가 (비관적 락)
            ProductCountEntity productCount = productCountRepository
                .findByProductIdWithPessimisticLock(criteria.productId())
                .orElseGet(() -> new ProductCountEntity(criteria.productId()));
            
            productCount.incrementLikeCount();
            productCountRepository.save(productCount);
            
            log.debug("좋아요 추가 완료 - 현재 카운트: {}", productCount.getLikeCount());
            
            // 3. 결과 반환
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(true, productCount.getLikeCount());
            return LikeResult.LikeToggleResult.from(domainInfo);
        } else {
            log.debug("중복 좋아요 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
            
            // 현재 카운트 조회
            Long likeCount = productCountRepository.findByProductId(criteria.productId())
                .map(ProductCountEntity::getLikeCount)
                .orElse(0L);
            
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(true, likeCount);
            return LikeResult.LikeToggleResult.from(domainInfo);
        }
    }
    
    /**
     * 좋아요 삭제
     */
    @Transactional
    public LikeResult.LikeToggleResult removeLike(LikeCriteria.RemoveLike criteria) {
        log.debug("좋아요 삭제 시작 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
        
        // 1. 좋아요 삭제
        LikeCommand.Toggle command = criteria.toCommand();
        boolean removed = likeService.removeLike(command);
        
        if (removed) {
            // 2. 카운트 감소 (비관적 락)
            ProductCountEntity productCount = productCountRepository
                .findByProductIdWithPessimisticLock(criteria.productId())
                .orElseThrow(() -> new IllegalStateException("상품 카운트 정보가 없습니다."));
            
            // 음수 방지
            if (productCount.getLikeCount() > 0) {
                productCount.decrementLikeCount();
                productCountRepository.save(productCount);
            }
            
            log.debug("좋아요 삭제 완료 - 현재 카운트: {}", productCount.getLikeCount());
            
            // 3. 결과 반환
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(false, productCount.getLikeCount());
            return LikeResult.LikeToggleResult.from(domainInfo);
        } else {
            log.debug("삭제할 좋아요 없음 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
            
            // 현재 카운트 조회
            Long likeCount = productCountRepository.findByProductId(criteria.productId())
                .map(ProductCountEntity::getLikeCount)
                .orElse(0L);
            
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(false, likeCount);
            return LikeResult.LikeToggleResult.from(domainInfo);
        }
    }
    
    /**
     * 사용자가 좋아요한 상품 목록 조회
     */
    public Page<LikeResult.LikedProduct> getLikedProducts(LikeCriteria.GetLikedProducts criteria) {
        LikeCommand.GetList command = criteria.toCommand();
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size());
        
        // 좋아요한 상품 목록 조회
        Page<LikedProductDto> likedProducts = likeService.getLikedProducts(command, pageRequest);
        
        return likedProducts.map(dto -> new LikeResult.LikedProduct(
            dto.getProductId(),
            dto.getBrandId(),
            dto.getBrandNameKo(),
            dto.getProductNameKo(),
            dto.getDescription(),
            dto.getPrice(),
            dto.getLikeCount(),
            dto.isAvailable(),
            dto.getLikedAt()
        ));
    }
}
