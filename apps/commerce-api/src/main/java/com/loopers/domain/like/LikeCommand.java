package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class LikeCommand {
    
    public record Toggle(
        String userId,
        Long productId
    ) {
        public Toggle {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
    
    public record GetList(
        String userId,
        Integer page,
        Integer size
    ) {
        public GetList {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (page == null || page < 0) {
                page = 0;
            }
            if (size == null || size <= 0) {
                size = 20;
            }
            if (size > 100) {
                throw new CoreException(ErrorType.BAD_REQUEST, "페이지 크기는 최대 100개까지 가능합니다.");
            }
        }
        
        public static GetList of(String userId, Integer page, Integer size) {
            return new GetList(userId, page, size);
        }
    }
    
    public record IsLiked(
        String userId,
        Long productId
    ) {
        public IsLiked {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
}
