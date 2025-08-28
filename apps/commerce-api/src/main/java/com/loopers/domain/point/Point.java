package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.vo.Charge;
import com.loopers.domain.point.vo.TransactionType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "points")
public class Point extends BaseEntity {
    
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance", nullable = false))
    private Money balance;

    public Point(String userId, Money initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
    }

    public PointHistory charge(Charge chargeAmount) {
        Money amount = Money.of(chargeAmount.value());
        Money newBalance = balance.add(amount);
        this.balance = newBalance;
        
        return new PointHistory(
            userId,
            amount,
            TransactionType.CHARGE,
            "포인트 충전",
            null,
            newBalance
        );
    }
    
    public PointHistory use(Money amount, Long orderId) {
        if (canPay(amount)) {
            Money newBalance = balance.subtract(amount);
            this.balance = newBalance;
            
            return new PointHistory(
                userId,
                amount,
                TransactionType.USE,
                "주문 결제 - 주문번호: " + orderId,
                orderId,
                newBalance
            );
        }
        
        throw new CoreException(ErrorType.CONFLICT, "포인트가 부족합니다.");
    }
    
    public boolean canPay(Money amount) {
        return balance.isGreaterThanOrEqual(amount);
    }
    
    public PointHistory refund(Money amount, Long orderId) {
        Money newBalance = balance.add(amount);
        this.balance = newBalance;
        
        return new PointHistory(
            userId,
            amount,
            TransactionType.REFUND,
            "주문 취소 환불 - 주문번호: " + orderId,
            orderId,
            newBalance
        );
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Money getBalance() {
        return balance;
    }

    public static Point createInitial(String userId) {
        return new Point(userId, Money.ZERO);
    }
    
    @Override
    protected void guard() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (balance == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "잔액은 필수입니다.");
        }
    }
}
