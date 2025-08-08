package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeInfo;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCountCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductCountService;
import com.loopers.domain.product.ProductStockService;
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
public class LikeFacade {
    
    private final LikeService likeService;
    private final LikeJpaRepository likeJpaRepository;
    private final ProductService productService;
    private final ProductCountService productCountService;
    private final ProductStockService productStockService;
    
    @Transactional
    public LikeResult.LikeToggleResult addLike(LikeCriteria.AddLike criteria) {
        LikeCommand.Toggle command = criteria.toCommand();
        boolean wasAdded = likeService.addLike(command);
        
        // 실제로 추가된 경우
        if (wasAdded) {
            ProductCountCommand.UpdateLikeCount updateCommand = new ProductCountCommand.UpdateLikeCount(criteria.productId());
            productCountService.updateLikeCount(updateCommand);
        }
        
        ProductCountCommand.GetLikeCount getCommand = new ProductCountCommand.GetLikeCount(criteria.productId());
        Long likeCount = productCountService.getLikeCount(getCommand);
        LikeCommand.IsLiked isLikedCommand = criteria.toIsLikedCommand();
        boolean isLiked = likeService.isLiked(isLikedCommand);
        
        LikeInfo.LikeResult domainInfo = LikeInfo.LikeResult.of(isLiked, likeCount);
        return LikeResult.LikeToggleResult.from(domainInfo);
    }
    
    @Transactional
    public LikeResult.LikeToggleResult removeLike(LikeCriteria.RemoveLike criteria) {
        // 삭제 전 상태 확인
        LikeCommand.IsLiked isLikedCommand = criteria.toIsLikedCommand();
        boolean wasLiked = likeService.isLiked(isLikedCommand);
        
        LikeCommand.Toggle command = criteria.toCommand();
        likeService.removeLike(command);
        
        // 실제로 삭제된 경우
        if (wasLiked) {
            ProductCountCommand.UpdateLikeCount updateCommand = new ProductCountCommand.UpdateLikeCount(criteria.productId());
            productCountService.updateLikeCount(updateCommand);
        }
        
        ProductCountCommand.GetLikeCount getCommand = new ProductCountCommand.GetLikeCount(criteria.productId());
        Long likeCount = productCountService.getLikeCount(getCommand);
        
        LikeInfo.LikeResult domainInfo = LikeInfo.LikeResult.of(false, likeCount);
        return LikeResult.LikeToggleResult.from(domainInfo);
    }
    
    public Page<LikeResult.LikedProduct> getLikedProducts(LikeCriteria.GetLikedProducts criteria) {
        // 1. 페이징 설정
        Pageable pageable = PageRequest.of(
            criteria.page(), 
            criteria.size(), 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        // 2. 좋아요 목록 조회
        Page<LikeEntity> likes = likeJpaRepository.findByUserId(criteria.userId(), pageable);
        
        if (likes.isEmpty()) {
            return likes.map(like -> null);
        }
        
        // 3. 상품 정보 조회 및 조합
        List<Long> productIds = likes.getContent().stream()
            .map(LikeEntity::getProductId)
            .toList();
        
        // 상품별로 Product + Brand 정보를 함께 조회
        Map<Long, ProductService.ProductWithBrand> productWithBrandMap = productIds.stream()
            .collect(Collectors.toMap(
                productId -> productId,
                productId -> {
                    try {
                        ProductCommand.GetOne command = new ProductCommand.GetOne(productId);
                        return productService.getProductWithBrand(command);
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
            ProductService.ProductWithBrand productWithBrand = 
                productWithBrandMap.get(like.getProductId());
            
            if (productWithBrand == null) {
                return null; // 상품이 삭제된 경우
            }
            
            var product = productWithBrand.product();
            var brand = productWithBrand.brand();
            Long likeCount = likeCountMap.getOrDefault(product.getId(), 0L);
            
            LikeInfo.LikedProduct domainInfo = new LikeInfo.LikedProduct(
                product.getId(),
                product.getBrandId(),
                brand.getNameKo(),
                product.getNameKo(),
                product.getDescription(),
                product.getPrice().amount(),
                likeCount,
                productStockService.isAvailable(product.getId()),
                like.getCreatedAt()
            );
            
            return LikeResult.LikedProduct.from(domainInfo);
        });
    }
}
