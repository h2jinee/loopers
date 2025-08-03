package com.loopers.domain.common;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money {
    
    private final BigDecimal amount;
    
    private Money(BigDecimal amount) {
        if (amount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 null일 수 없습니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 0원 이상이어야 합니다.");
        }
        this.amount = amount;
    }
    
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
    
    public static Money of(int amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    
    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    
    public static Money ZERO() {
        return new Money(BigDecimal.ZERO);
    }
    
    public BigDecimal amount() {
        return amount;
    }
    
    public Money add(Money other) {
        if (other == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "추가 금액이 null 입니다.");
        }
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        if (other == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감 금액이 null 입니다.");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 음수가 될 수 없습니다.");
        }
        return new Money(result);
    }
    
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0 이상이어야 합니다.");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        if (other == null) {
            return true;
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return amount.toString() + "원";
    }
}
