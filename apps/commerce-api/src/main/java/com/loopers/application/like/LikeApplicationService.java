package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeDomainService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCountCommand;
import com.loopers.domain.product.ProductDomainService;
import com.loopers.domain.product.ProductCountService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeApplicationService {
    
    private final LikeDomainService likeDomainService;
    private final LikeRepository likeRepository;
    private final ProductDomainService productDomainService;
    private final ProductCountService productCountService;
    
    @Transactional
    public LikeInfo.Result addLike(String userId, Long productId) {
        LikeCommand.Toggle command = new LikeCommand.Toggle(userId, productId);
        boolean wasAdded = likeDomainService.addLike(command);
        
        // 실제로 추가된 경우
        if (wasAdded) {
            ProductCountCommand.UpdateLikeCount updateCommand = new ProductCountCommand.UpdateLikeCount(productId);
            productCountService.updateLikeCount(updateCommand);
        }
        
        ProductCountCommand.GetLikeCount getCommand = new ProductCountCommand.GetLikeCount(productId);
        Long likeCount = productCountService.getLikeCount(getCommand);
        LikeCommand.IsLiked isLikedCommand = new LikeCommand.IsLiked(userId, productId);
        boolean isLiked = likeDomainService.isLiked(isLikedCommand);
        return LikeInfo.Result.of(isLiked, likeCount);
    }
    
    @Transactional
    public LikeInfo.Result removeLike(String userId, Long productId) {
        // 삭제 전 상태 확인
        LikeCommand.IsLiked isLikedCommand = new LikeCommand.IsLiked(userId, productId);
        boolean wasLiked = likeDomainService.isLiked(isLikedCommand);
        
        LikeCommand.Toggle command = new LikeCommand.Toggle(userId, productId);
        likeDomainService.removeLike(command);
        
        // 실제로 삭제된 경우
        if (wasLiked) {
            ProductCountCommand.UpdateLikeCount updateCommand = new ProductCountCommand.UpdateLikeCount(productId);
            productCountService.updateLikeCount(updateCommand);
        }
        
        ProductCountCommand.GetLikeCount getCommand = new ProductCountCommand.GetLikeCount(productId);
        Long likeCount = productCountService.getLikeCount(getCommand);
        return LikeInfo.Result.of(false, likeCount);
    }
    
    public Page<LikeInfo.LikedProduct> getLikedProducts(String userId, Integer page, Integer size) {
        // 1. 페이징 설정
        Pageable pageable = PageRequest.of(
            page, 
            size, 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        // 2. 좋아요 목록 조회
        Page<LikeEntity> likes = likeRepository.findByUserId(userId, pageable);
        
        if (likes.isEmpty()) {
            return likes.map(like -> null);
        }
        
        // 3. 상품 정보 조회 및 조합
        List<Long> productIds = likes.getContent().stream()
            .map(LikeEntity::getProductId)
            .toList();
        
        // 상품별로 Product + Brand 정보를 함께 조회
        Map<Long, ProductDomainService.ProductWithBrand> productWithBrandMap = productIds.stream()
            .collect(Collectors.toMap(
                productId -> productId,
                productId -> {
                    try {
                        ProductCommand.GetOne command = new ProductCommand.GetOne(productId);
                        return productDomainService.getProductWithBrand(command);
                    } catch (CoreException e) {
                        return null;
                    }
                }
            ));
        
        // 좋아요 수 조회
        Map<Long, Long> likeCountMap = productIds.stream()
            .collect(Collectors.toMap(
                productId -> productId,
                productId -> {
                    ProductCountCommand.GetLikeCount command = new ProductCountCommand.GetLikeCount(productId);
                    return productCountService.getLikeCount(command);
                }
            ));
        
        // 4. 결과 조합
        return likes.map(like -> {
            ProductDomainService.ProductWithBrand productWithBrand = 
                productWithBrandMap.get(like.getProductId());
            
            if (productWithBrand == null) {
                return null; // 상품이 삭제된 경우
            }
            
            var product = productWithBrand.product();
            var brand = productWithBrand.brand();
            Long likeCount = likeCountMap.getOrDefault(product.getId(), 0L);
            
            return new LikeInfo.LikedProduct(
                product.getId(),
                product.getBrandId(),
                brand.getNameKo(),
                product.getNameKo(),
                product.getDescription(),
                product.getPrice().amount(),
                likeCount,
                product.isAvailable(),
                like.getCreatedAt()
            );
        });
    }
}
