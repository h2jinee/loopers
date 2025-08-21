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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    
    @Getter
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Getter
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", 
            column = @Column(name = "total_amount", nullable = false))
    })
    private Money totalAmount;
    
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Getter
    @Embedded
    private ReceiverInfo receiverInfo;
    
    @Getter
    @Column(name = "payment_deadline")
    private ZonedDateTime paymentDeadline;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<OrderLine> orderLines = new ArrayList<>();
    
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;
    
    public Order(String userId, ReceiverInfo receiverInfo) {
        this.userId = userId;
        this.receiverInfo = receiverInfo;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.totalAmount = Money.zero();
        this.paymentDeadline = ZonedDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
    }
    
    public void addOrderLine(Long productId, String productName, Integer quantity, Money price) {
        if (productId == null || productName == null || quantity == null || price == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상품 정보가 올바르지 않습니다.");
        }
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
        }
        
        OrderLine orderLine = new OrderLine(this, productId, productName, quantity, price);
        this.orderLines.add(orderLine);
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        Money sum = Money.zero();
        for (OrderLine line : orderLines) {
            sum = sum.add(line.getSubtotal());
        }
        this.totalAmount = sum;
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
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
        if (totalAmount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 금액은 필수입니다.");
        }
    }
}
