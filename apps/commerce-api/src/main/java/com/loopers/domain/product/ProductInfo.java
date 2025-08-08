package com.loopers.domain.product;

import com.loopers.domain.product.vo.ProductStatus;

import java.math.BigDecimal;

public class ProductInfo {
    
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
        public static Detail from(ProductEntity product, String brandName, ProductStockService stockService) {
            return new Detail(
                product.getId(),
                product.getBrandId(),
                brandName,
                product.getNameKo(),
                product.getDescription(),
                product.getPrice().amount(),
                product.getShippingFee().amount(),
                product.getTotalPrice().amount(),
                stockService.getStock(product.getId()),
                product.getStatus(),
                product.getReleaseYear(),
                product.getLikeCount(),
                stockService.isAvailable(product.getId())
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
        public static Summary from(ProductEntity product, String brandName, ProductStockService stockService) {
            return new Summary(
                product.getId(),
                product.getBrandId(),
                brandName,
                product.getNameKo(),
                product.getDescription(),
                product.getPrice().amount(),
                product.getLikeCount(),
                stockService.isAvailable(product.getId())
            );
        }
    }
}