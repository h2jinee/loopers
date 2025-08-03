package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class BrandCommand {
    
    public record GetList(
        Integer page,
        Integer size
    ) {
        public GetList {
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
        
        public static GetList of(Integer page, Integer size) {
            return new GetList(page, size);
        }
    }
    
    public record GetOne(
        Long brandId
    ) {
        public GetOne {
            if (brandId == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 필수입니다.");
            }
        }
    }
}
