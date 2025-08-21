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
@Table(name = "stock_reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservation extends BaseEntity {
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    public enum ReservationStatus {
        RESERVED("예약됨"),
        CONFIRMED("확정됨"),
        CANCELLED("취소됨");
        
        private final String description;
        
        ReservationStatus(String description) {
            this.description = description;
        }
    }
    
    public StockReservation(Long orderId, Long productId, Integer quantity) {
        validateOrderId(orderId);
        validateProductId(productId);
        validateQuantity(quantity);
        
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
    }
    
    // 예약 확정 (결제 완료 시)
    public void confirm() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                "예약 상태가 아닌 건은 확정할 수 없습니다. 현재 상태: " + this.status);
        }
        this.status = ReservationStatus.CONFIRMED;
    }
    
    // 예약 취소 (결제 실패 시)
    public void cancel() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                "예약 상태가 아닌 건은 취소할 수 없습니다. 현재 상태: " + this.status);
        }
        this.status = ReservationStatus.CANCELLED;
    }
    
    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }
    
    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return this.status == ReservationStatus.CANCELLED;
    }
    
    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
        }
    }
    
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "예약 수량은 1 이상이어야 합니다.");
        }
    }
    
    @Override
    protected void guard() {
        validateOrderId(this.orderId);
        validateProductId(this.productId);
        validateQuantity(this.quantity);
        
        if (this.status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "예약 상태는 필수입니다.");
        }
    }
}
