package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import java.util.List;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record PageCacheDocument(
    List<ProductCacheDocument> content,
    long totalElements,
    int totalPages,
    int pageNumber,
    int pageSize
) {
    public static PageCacheDocument from(Page<Product> page) {
        return PageCacheDocument.builder()
            .content(page.getContent().stream()
                .map(ProductCacheDocument::from)
                .toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .build();
    }
}
