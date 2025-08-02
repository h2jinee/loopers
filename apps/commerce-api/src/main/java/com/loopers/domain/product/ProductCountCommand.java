package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class ProductCountCommand {
    
    public record UpdateLikeCount(
        Long productId
    ) {
        public UpdateLikeCount {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
    
    public record GetLikeCount(
        Long productId
    ) {
        public GetLikeCount {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
}
