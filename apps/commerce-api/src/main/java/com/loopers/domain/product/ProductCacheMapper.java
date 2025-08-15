package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCacheMapper {
    
    /**
     * 캐시된 상품 목록 Page 객체로 변환
     */
    public Page<ProductWithBrandDto> convertToPage(ProductListCacheDto cacheDto, Pageable pageable) {
        // ProductListInfo의 팩토리 메서드를 활용하여 변환
        List<ProductListInfo.ProductListItem> items = cacheDto.products().stream()
            .map(item -> new ProductListInfo.ProductListItem(
                item.productId(),
                item.brandId(),
                item.brandNameKo(),
                item.brandNameEn(),
                item.productNameKo(),
                item.description(),
                item.price(),
                item.shippingFee(),
                item.status(),
                item.releaseYear(),
                item.likeCount()
            ))
            .toList();
        
        ProductListInfo listInfo = new ProductListInfo(
            items,
            cacheDto.totalPages(),
            cacheDto.totalElements(),
            cacheDto.currentPage(),
            cacheDto.pageSize()
        );
        
        return listInfo.toPage(pageable);
    }
}
