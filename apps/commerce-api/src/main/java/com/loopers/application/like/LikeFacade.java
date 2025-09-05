package com.loopers.application.like;

import com.loopers.application.event.EventPublisher;
import com.loopers.application.event.like.LikeEvent;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductCountService;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;
    private final BrandService brandService;
    private final ProductCountService productCountService;
	private final EventPublisher eventPublisher;

    /**
     * 좋아요 추가
     */
    @Transactional
    public LikeResult.LikeToggleResult addLike(LikeCriteria.AddLike criteria) {
        log.debug("좋아요 추가 시작 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
        
        // 1. 좋아요 추가 (메인 로직)
        LikeCommand.Toggle command = criteria.toCommand();
        boolean added = likeService.addLike(command);
        
        if (added) {
            // 2. 좋아요 추가 이벤트 발행
			LikeEvent.Added event = LikeEvent.Added.from(criteria.userId(), criteria.productId());
			eventPublisher.publish(event);
            
            log.debug("좋아요 추가 완료 및 이벤트 발행 - userId: {}, productId: {}", 
                criteria.userId(), criteria.productId());
            
            // 3. 현재 카운트 조회
            Long currentCount = productCountService.getLikeCount(criteria.productId());
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(true, currentCount);
            return LikeResult.LikeToggleResult.from(domainInfo);
            
        } else {
            log.debug("중복 좋아요 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
            
            // 현재 카운트 조회 (읽기만)
            Long likeCount = productCountService.getLikeCount(criteria.productId());
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
        
        // 1. 좋아요 삭제 (메인 로직)
        LikeCommand.Toggle command = criteria.toCommand();
        boolean removed = likeService.removeLike(command);
        
        if (removed) {
            // 2. 좋아요 삭제 이벤트 발행
			LikeEvent.Removed event = LikeEvent.Removed.from(criteria.userId(), criteria.productId());
			eventPublisher.publish(event);
            
            log.debug("좋아요 삭제 완료 및 이벤트 발행 - userId: {}, productId: {}", 
                criteria.userId(), criteria.productId());
            
            // 3. 현재 카운트 조회 (읽기만)
            Long currentCount = productCountService.getLikeCount(criteria.productId());
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(false, currentCount);
            return LikeResult.LikeToggleResult.from(domainInfo);
            
        } else {
            log.debug("삭제할 좋아요 없음 - userId: {}, productId: {}", criteria.userId(), criteria.productId());
            
            // 현재 카운트 조회
            Long likeCount = productCountService.getLikeCount(criteria.productId());
            
            LikeInfo.LikeResult domainInfo = new LikeInfo.LikeResult(false, likeCount);
            return LikeResult.LikeToggleResult.from(domainInfo);
        }
    }
    
    /**
     * 사용자가 좋아요한 상품 목록 조회
     */
    public Page<LikeResult.LikedProduct> getLikedProducts(LikeCriteria.GetLikedProducts criteria) {
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size());
        
        // 1. Like 엔티티만 조회 (도메인 경계 유지)
        Page<Like> likes = likeService.getUserLikes(criteria.userId(), pageRequest);
        
        if (likes.isEmpty()) {
            return Page.empty(pageRequest);
        }
        
        // 2. 상품 ID 추출
        List<Long> productIds = likes.stream()
            .map(Like::getProductId)
            .collect(Collectors.toList());
        
        // 3. 상품 정보 일괄 조회 (N+1 방지)
        List<Product> products = productService.getProductsByIds(productIds);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 4. 브랜드 ID 추출 및 브랜드 정보 일괄 조회
        List<Long> brandIds = products.stream()
            .map(Product::getBrandId)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, BrandInfo> brandMap = brandService.getBrandsByIds(brandIds);
        
        // 5. 조합하여 Result 생성
        List<LikeResult.LikedProduct> likedProducts = likes.stream()
            .map(like -> {
                Product product = productMap.get(like.getProductId());
                if (product == null) {
                    log.warn("좋아요한 상품을 찾을 수 없음 - productId: {}", like.getProductId());
                    return null;
                }
                
                BrandInfo brand = brandMap.get(product.getBrandId());
                if (brand == null) {
                    log.warn("브랜드를 찾을 수 없음 - brandId: {}", product.getBrandId());
                    return null;
                }
                
                // LikeInfo.LikedProduct 생성 후 LikeResult.LikedProduct로 변환
                LikeInfo.LikedProduct likeInfo = LikeInfo.LikedProduct.of(
                    product.getId(),
                    brand.brandId(),
                    brand.nameKo(),
                    product.getNameKo(),
                    product.getDescription(),
                    product.getPrice().amount(),
                    product.getLikeCount(),
                    product.getStatus(),
                    like.getCreatedAt()
                );
                
                return LikeResult.LikedProduct.from(likeInfo);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        return new PageImpl<>(likedProducts, pageRequest, likes.getTotalElements());
    }
}
