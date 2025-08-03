package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.vo.OrderStatus;
import com.loopers.domain.order.vo.ReceiverInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Embedded
    private ReceiverInfo receiverInfo;
    
    @Column(name = "payment_deadline")
    private ZonedDateTime paymentDeadline;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLineEntity> orderLines = new ArrayList<>();
    
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;
    
    public OrderEntity(
        String userId,
        ReceiverInfo receiverInfo
    ) {
        this.userId = userId;
        this.receiverInfo = receiverInfo;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.paymentDeadline = ZonedDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
    }
    
    public void addOrderLine(OrderLineEntity orderLine) {
        this.orderLines.add(orderLine);
        orderLine.setOrder(this);
        recalculateTotalAmount();
    }
    
    public void removeOrderLine(OrderLineEntity orderLine) {
        this.orderLines.remove(orderLine);
        orderLine.setOrder(null);
        recalculateTotalAmount();
    }
    
    private void recalculateTotalAmount() {
        this.totalAmount = orderLines.stream()
            .map(line -> line.getSubtotal().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Money getTotalAmount() {
        return Money.of(totalAmount);
    }
    
    public boolean isPaymentExpired() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            return false;
        }
        return ZonedDateTime.now().isAfter(paymentDeadline);
    }
    
    public void confirmPayment() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 상태가 아닙니다.");
        }
        if (isPaymentExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 시간이 만료되었습니다.");
        }
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }
    
    public void failPayment() {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 상태가 아닙니다.");
        }
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    @Override
    protected void guard() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (receiverInfo == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수령인 정보는 필수입니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태는 필수입니다.");
        }
    }
}
