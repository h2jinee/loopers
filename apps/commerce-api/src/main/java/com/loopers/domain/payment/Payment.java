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
    private String transactionKey;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    private String failureReason;
    
    private Payment(
        Long orderId,
        String userId,
        Money amount,
        PaymentMethod paymentMethod,
        String transactionKey
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionKey = transactionKey;
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
        String transactionKey
    ) {
        return new Payment(orderId, userId, amount, PaymentMethod.PG, transactionKey);
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
     * 트랜잭션 ID 업데이트 (TEMP ID를 실제 ID로 교체)
     */
    public void updateTransactionKey(String transactionKey) {
        if (transactionKey == null || transactionKey.isBlank()) {
            throw new IllegalArgumentException("트랜잭션 ID는 필수입니다.");
        }
        this.transactionKey = transactionKey;
    }

    public void setTransactionKey(String transactionKey) {
        updateTransactionKey(transactionKey);
    }
    
    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
}
