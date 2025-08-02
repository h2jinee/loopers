package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class ProductCommand {
    
    public enum SortType {
        LATEST("latest"),
        PRICE_ASC("price_asc"),
        LIKES_DESC("likes_desc");
        
        private final String value;
        
        SortType(String value) {
            this.value = value;
        }
        
        public static SortType from(String value) {
            if (value == null) {
                return LATEST;
            }
            
            for (SortType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 정렬 방식입니다.");
        }
    }
    
    public record GetList(
        Long brandId,
        SortType sort,
        Integer page,
        Integer size
    ) {
        public GetList {
            if (sort == null) {
                sort = SortType.LATEST;
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
        
        public static GetList of(Long brandId, String sort, Integer page, Integer size) {
            return new GetList(
                brandId,
                SortType.from(sort),
                page != null ? page : 0,
                size != null ? size : 20
            );
        }
    }
    
    public record DecreaseStock(
        Long productId,
        Integer quantity
    ) {
        public DecreaseStock {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (quantity == null || quantity <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
        }
    }
    
    public record IncreaseStock(
        Long productId,
        Integer quantity
    ) {
        public IncreaseStock {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (quantity == null || quantity <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
        }
    }
    
    public record GetOne(
        Long productId
    ) {
        public GetOne {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
    
    public record ValidateExists(
        Long productId
    ) {
        public ValidateExists {
            if (productId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
        }
    }
}
