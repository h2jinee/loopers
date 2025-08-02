package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name = "stock_reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservationEntity extends BaseEntity {
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    @Column(name = "expired_at", nullable = false)
    private ZonedDateTime expiredAt;
    
    private static final int RESERVATION_TIMEOUT_MINUTES = 30;
    
    public enum ReservationStatus {
        RESERVED,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }
    
    public StockReservationEntity(
        Long orderId,
        Long productId,
        Integer quantity
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.expiredAt = ZonedDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES);
    }
    
    public boolean isExpired() {
        if (status != ReservationStatus.RESERVED) {
            return false;
        }
        return ZonedDateTime.now().isAfter(expiredAt);
    }
    
    public void confirm() {
        if (status != ReservationStatus.RESERVED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "예약된 상태가 아닙니다.");
        }
        if (isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "예약이 만료되었습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }
    
    public void cancel() {
        if (status == ReservationStatus.CONFIRMED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 확정된 예약은 취소할 수 없습니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }
    
    @Override
    protected void guard() {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
        }
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "예약 상태는 필수입니다.");
        }
    }
}
