package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private String userId;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(unique = true)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    private String failureReason;
    
    private Payment(
        Long orderId,
        String userId,
        Money amount,
        PaymentMethod paymentMethod,
        String transactionId
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = PaymentStatus.PENDING;
    }
    
    /**
     * 포인트 결제 생성
     */
    public static Payment createPointPayment(Long orderId, String userId, Money amount) {
        return new Payment(orderId, userId, amount, PaymentMethod.POINT, null);
    }
    
    /**
     * PG 결제 생성
     */
    public static Payment createPgPayment(
        Long orderId,
        String userId,
        Money amount,
        String transactionId
    ) {
        return new Payment(orderId, userId, amount, PaymentMethod.PG, transactionId);
    }
    
    /**
     * 포인트 결제용 임시 Payment 객체 생성 (취소용)
     */
    public static Payment forPoint(Long orderId, String userId, Money amount) {
        return createPointPayment(orderId, userId, amount);
    }
    
    /**
     * 결제 완료 처리
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.COMPLETED;
    }
    
    /**
     * 결제 실패 처리
     */
    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 실패 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
    
    /**
     * 결제 취소 처리
     */
    public void cancel() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }
    
    public boolean isCancelled() {
        return this.status == PaymentStatus.CANCELLED;
    }
}
