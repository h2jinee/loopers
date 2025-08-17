package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record ProductListCacheDto(
    List<ProductListItem> products,
    int totalPages,
    long totalElements,
    int currentPage,
    int pageSize
) {
    public record ProductListItem(
        Long productId,
        Long brandId,
        String brandNameKo,
        String brandNameEn,
        String productNameKo,
        String description,
        BigDecimal price,
        BigDecimal shippingFee,
        ProductStatus status,
        Integer releaseYear,
        Long likeCount
    ) {
        public static ProductListItem from(ProductWithBrandDto dto) {
            return new ProductListItem(
                dto.productId(),
                dto.brandId(),
                dto.brandNameKo(),
                dto.brandNameEn(),
                dto.productNameKo(),
                dto.description(),
                dto.price(),
                dto.shippingFee(),
                dto.status(),
                dto.releaseYear(),
                dto.likeCount()
            );
        }
    }
    
    public static ProductListCacheDto from(org.springframework.data.domain.Page<ProductWithBrandDto> page) {
        List<ProductListItem> items = page.getContent().stream()
            .map(ProductListItem::from)
            .toList();
            
        return new ProductListCacheDto(
            items,
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize()
        );
    }
}
