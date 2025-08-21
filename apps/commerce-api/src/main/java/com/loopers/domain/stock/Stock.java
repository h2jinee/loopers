package com.loopers.domain.stock;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {
    
    @Column(name = "product_id", unique = true, nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    public Stock(Long productId, Integer initialQuantity) {
        validateProductId(productId);
        validateQuantity(initialQuantity);
        this.productId = productId;
        this.quantity = initialQuantity;
    }
    
    // 재고 차감
    public void decrease(Integer amount) {
        validateAmount(amount);
        if (this.quantity < amount) {
            throw new CoreException(ErrorType.CONFLICT, 
                String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", this.quantity, amount));
        }
        this.quantity -= amount;
    }
    
    // 재고 증가
    public void increase(Integer amount) {
        validateAmount(amount);
        this.quantity += amount;
    }
    
    // 재고 조정 (재고 실사 등)
    public void adjust(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }
    
    // 구매 가능 여부
    public boolean isAvailable(Integer requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }
    
    // 재고 부족 여부
    public boolean isOutOfStock() {
        return this.quantity <= 0;
    }
    
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다.");
        }
    }
    
    private void validateAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1 이상이어야 합니다.");
        }
    }
    
    @Override
    protected void guard() {
        validateProductId(this.productId);
        validateQuantity(this.quantity);
    }
}
