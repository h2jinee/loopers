package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.vo.PointTransactionType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "point_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryEntity extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionType type;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;
    
    public PointHistoryEntity(
        String userId,
        Money amount,
        PointTransactionType type,
        String description,
        Long orderId,
        Money balanceAfter
    ) {
        this.userId = userId;
        this.amount = amount.amount();
        this.type = type;
        this.description = description;
        this.orderId = orderId;
        this.balanceAfter = balanceAfter.amount();
    }
    
    public Money getAmount() {
        return Money.of(amount);
    }

    @Override
    protected void guard() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (amount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 필수입니다.");
        }
        if (type == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "거래 유형은 필수입니다.");
        }
        if (balanceAfter == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "거래 후 잔액은 필수입니다.");
        }
    }
}
