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
                Long productId,
                Long brandId,
                String brandNameKo,
                String productNameKo,
                String description,
                BigDecimal price,
                Long likeCount,
                boolean isAvailable
            ) {
                public static Response from(ProductResult.Summary summary) {
                    return new Response(
                        summary.productId(),
                        summary.brandId(),
                        summary.brandNameKo(),
                        summary.productNameKo(),
                        summary.description(),
                        summary.price(),
                        summary.likeCount(),
                        summary.isAvailable()
                    );
                }
            }
        }
        
        public static class GetDetail {
            public record Response(
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
                public static Response from(ProductResult.Detail detail) {
                    return new Response(
                        detail.productId(),
                        detail.brandId(),
                        detail.brandNameKo(),
                        detail.productNameKo(),
                        detail.description(),
                        detail.price(),
                        detail.shippingFee(),
                        detail.totalPrice(),
                        detail.stock(),
                        detail.status(),
                        detail.releaseYear(),
                        detail.likeCount(),
                        detail.isAvailable()
                    );
                }
            }
        }
    }
}
