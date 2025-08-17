package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public record ProductListInfo(
    List<ProductListItem> items,
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
        
        public ProductWithBrandDto toDto() {
            return new ProductWithBrandDto(
                productId,
                brandId,
                productNameKo,
                price,
                description,
                status,
                releaseYear,
                shippingFee,
                likeCount,
                brandNameKo,
                brandNameEn
            );
        }
    }
    
    public static ProductListInfo from(Page<ProductWithBrandDto> page) {
        List<ProductListItem> items = page.getContent().stream()
            .map(ProductListItem::from)
            .toList();
            
        return new ProductListInfo(
            items,
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize()
        );
    }
    
    public Page<ProductWithBrandDto> toPage(Pageable pageable) {
        List<ProductWithBrandDto> content = items.stream()
            .map(ProductListItem::toDto)
            .toList();
            
        return new PageImpl<>(content, pageable, totalElements);
    }
}
