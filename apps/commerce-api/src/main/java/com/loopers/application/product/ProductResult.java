package com.loopers.application.product;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.vo.ProductStatus;

import java.math.BigDecimal;

public class ProductResult {
    
    public record Detail(
        Long productId,
        Long brandId,
        String brandNameKo,
        String productNameKo,
        String description,
        BigDecimal price,
        BigDecimal shippingFee,
        BigDecimal totalPrice,
        Integer stock,
        ProductStatus status,
        Integer releaseYear,
        Long likeCount,
        boolean isAvailable
    ) {
        public static Detail from(ProductInfo.Detail domainInfo) {
            return new Detail(
                domainInfo.productId(),
                domainInfo.brandId(),
                domainInfo.brandNameKo(),
                domainInfo.productNameKo(),
                domainInfo.description(),
                domainInfo.price(),
                domainInfo.shippingFee(),
                domainInfo.totalPrice(),
                domainInfo.stock(),
                domainInfo.status(),
                domainInfo.releaseYear(),
                domainInfo.likeCount(),
                domainInfo.isAvailable()
            );
        }
    }
    
    public record Summary(
        Long productId,
        Long brandId,
        String brandNameKo,
        String productNameKo,
        String description,
        BigDecimal price,
        Long likeCount,
        boolean isAvailable
    ) {
        public static Summary from(ProductInfo.Summary domainInfo) {
            return new Summary(
                domainInfo.productId(),
                domainInfo.brandId(),
                domainInfo.brandNameKo(),
                domainInfo.productNameKo(),
                domainInfo.description(),
                domainInfo.price(),
                domainInfo.likeCount(),
                domainInfo.isAvailable()
            );
        }
    }
}