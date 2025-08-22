package com.loopers.application.product;

import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.stock.StockInfo;
import java.math.BigDecimal;

public class ProductResult {
    
    public record Detail(
        ProductDetail product,
        BrandDetail brand,
        StockDetail stock
    ) {
        public record ProductDetail(
            Long productId,
            String nameKo,
            String description,
            BigDecimal price,
            BigDecimal shippingFee,
            BigDecimal totalPrice,
            ProductStatus status,
            Integer releaseYear,
            Long likeCount,
            boolean isAvailable
        ) {
            public static ProductDetail from(ProductInfo info) {
                return new ProductDetail(
                    info.productId(),
                    info.nameKo(),
                    info.description(),
                    info.price().amount(),
                    info.shippingFee().amount(),
                    info.totalPrice().amount(),
                    info.status(),
                    info.releaseYear(),
                    info.likeCount(),
                    info.isAvailable()
                );
            }
        }
        
        public record BrandDetail(
            Long brandId,
            String nameKo
        ) {
            public static BrandDetail from(BrandInfo info) {
                return new BrandDetail(
                    info.brandId(),
                    info.nameKo()
                );
            }
        }
        
        public record StockDetail(
            Integer quantity,
            boolean inStock
        ) {
            public static StockDetail from(StockInfo info) {
                return new StockDetail(
                    info.quantity(),
                    info.quantity() > 0
                );
            }
        }
        
        public static Detail from(ProductInfo productInfo, BrandInfo brandInfo, StockInfo stockInfo) {
            return new Detail(
                ProductDetail.from(productInfo),
                BrandDetail.from(brandInfo),
                StockDetail.from(stockInfo)
            );
        }
        
        /**
         * Fallback 응답 생성
         * CircuitBreaker가 열렸을 때 반환할 기본값
         */
        public static Detail createFallback(Long productId) {
            ProductDetail fallbackProduct = new ProductDetail(
                productId,
                "일시적으로 조회 불가",
                "서비스가 일시적으로 불가능합니다",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ProductStatus.OUT_OF_STOCK,
                0,
                0L,
                false
            );
            
            BrandDetail fallbackBrand = new BrandDetail(
                0L,
                "Unknown"
            );
            
            StockDetail fallbackStock = new StockDetail(
                0,
                false
            );
            
            return new Detail(fallbackProduct, fallbackBrand, fallbackStock);
        }
    }
    
    public record Summary(
        ProductSummary product,
        BrandSummary brand,
        boolean isAvailable
    ) {
        public record ProductSummary(
            Long productId,
            String nameKo,
            String description,
            BigDecimal price,
            Long likeCount
        ) {
            public static ProductSummary from(ProductInfo info) {
                return new ProductSummary(
                    info.productId(),
                    info.nameKo(),
                    info.description(),
                    info.price().amount(),
                    info.likeCount()
                );
            }
        }
        
        public record BrandSummary(
            Long brandId,
            String nameKo
        ) {
            public static BrandSummary from(BrandInfo info) {
                if (info == null) {
                    return new BrandSummary(null, "Unknown");
                }
                return new BrandSummary(
                    info.brandId(),
                    info.nameKo()
                );
            }
        }
        
        public static Summary from(ProductInfo productInfo, BrandInfo brandInfo, StockInfo stockInfo) {
            return new Summary(
                ProductSummary.from(productInfo),
                BrandSummary.from(brandInfo),
                stockInfo != null && stockInfo.isAvailable()
            );
        }
    }
}
