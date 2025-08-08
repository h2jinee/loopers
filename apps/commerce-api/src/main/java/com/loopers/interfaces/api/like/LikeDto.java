package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class LikeDto {
    
    public static class V1 {
        
        public static class ToggleLike {
            public record Response(
                boolean isLiked,
                Long likeCount
            ) {
                public static Response from(LikeResult.LikeToggleResult result) {
                    return new Response(
                        result.isLiked(),
                        result.likeCount()
                    );
                }
            }
        }
        
        public static class GetLikedProducts {
            public record Response(
                Long productId,
                Long brandId,
                String brandNameKo,
                String productNameKo,
                String description,
                BigDecimal price,
                Long likeCount,
                boolean isAvailable,
                ZonedDateTime likedAt
            ) {
                public static Response from(LikeResult.LikedProduct product) {
                    return new Response(
                        product.productId(),
                        product.brandId(),
                        product.brandNameKo(),
                        product.productNameKo(),
                        product.description(),
                        product.price(),
                        product.likeCount(),
                        product.isAvailable(),
                        product.likedAt()
                    );
                }
            }
        }
    }
}
