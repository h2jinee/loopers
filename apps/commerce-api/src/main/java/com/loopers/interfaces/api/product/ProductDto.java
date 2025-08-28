package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.vo.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class ProductDto {
    
    public static class V1 {
        
        public static class GetList {
            public record Request(
                Long brandId,
                
                @Pattern(regexp = "^(latest|price_asc|likes_desc)$", 
                    message = "정렬 방식은 latest, price_asc, likes_desc 중 하나여야 합니다.")
                String sort,
                
                @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
                Integer page,
                
                @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
                Integer size
            ) {}
            
            public record Response(
                ProductInfo product,
                BrandInfo brand,
                boolean isAvailable
            ) {
                public record ProductInfo(
                    Long productId,
                    String nameKo,
                    String description,
                    BigDecimal price,
                    Long likeCount
                ) {}
                
                public record BrandInfo(
                    Long brandId,
                    String nameKo
                ) {}
                
                public static Response from(ProductResult.Summary summary) {
                    return new Response(
                        new ProductInfo(
                            summary.product().productId(),
                            summary.product().nameKo(),
                            summary.product().description(),
                            summary.product().price(),
                            summary.product().likeCount()
                        ),
                        new BrandInfo(
                            summary.brand().brandId(),
                            summary.brand().nameKo()
                        ),
                        summary.isAvailable()
                    );
                }
            }
        }
        
        public static class GetDetail {
            public record Response(
                ProductInfo product,
                BrandInfo brand,
                StockInfo stock
            ) {
                public record ProductInfo(
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
                ) {}
                
                public record BrandInfo(
                    Long brandId,
                    String nameKo
                ) {}
                
                public record StockInfo(
                    Integer quantity,
                    boolean inStock
                ) {}
                
                public static Response from(ProductResult.Detail detail) {
                    return new Response(
                        new ProductInfo(
                            detail.product().productId(),
                            detail.product().nameKo(),
                            detail.product().description(),
                            detail.product().price(),
                            detail.product().shippingFee(),
                            detail.product().totalPrice(),
                            detail.product().status(),
                            detail.product().releaseYear(),
                            detail.product().likeCount(),
                            detail.product().isAvailable()
                        ),
                        new BrandInfo(
                            detail.brand().brandId(),
                            detail.brand().nameKo()
                        ),
                        new StockInfo(
                            detail.stock().quantity(),
                            detail.stock().inStock()
                        )
                    );
                }
            }
        }
    }
}
