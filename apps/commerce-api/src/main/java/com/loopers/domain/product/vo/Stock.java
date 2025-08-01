package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Stock(Integer value) {
    public Stock {
        if (value == null || value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
        }
    }
    
    public Stock decrease(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감 수량은 1 이상이어야 합니다.");
        }
        
        int newValue = value - quantity;
        if (newValue < 0) {
            throw new CoreException(ErrorType.CONFLICT, "재고가 부족합니다.");
        }
        
        return new Stock(newValue);
    }
    
    public Stock increase(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "증가 수량은 1 이상이어야 합니다.");
        }
        
        return new Stock(value + quantity);
    }
    
    public boolean hasStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        return value >= quantity;
    }
}
