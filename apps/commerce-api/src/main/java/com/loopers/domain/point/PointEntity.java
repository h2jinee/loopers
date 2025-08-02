package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.vo.ChargePoint;
import com.loopers.domain.point.vo.PointTransactionType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "points")
public class PointEntity extends BaseEntity {
	@Id
	@Column(name = "user_id")
	private String userId;
	
	@Column(nullable = false)
	private BigDecimal balance;

	public PointEntity(String userId, Money initialBalance) {
		this.userId = userId;
		this.balance = initialBalance.amount();
	}

	public PointHistoryEntity charge(ChargePoint chargeAmount) {
		Money amount = Money.of(chargeAmount.value());
		Money newBalance = getBalance().add(amount);
		this.balance = newBalance.amount();
		
		return new PointHistoryEntity(
			userId,
			amount,
			PointTransactionType.CHARGE,
			"포인트 충전",
			null,
			newBalance
		);
	}
	
	public PointHistoryEntity use(Money amount, Long orderId) {
		if (canPay(amount)) {
			Money newBalance = getBalance().subtract(amount);
			this.balance = newBalance.amount();
			
			return new PointHistoryEntity(
				userId,
				amount,
				PointTransactionType.USE,
				"주문 결제 - 주문번호: " + orderId,
				orderId,
				newBalance
			);
		}
		
		throw new CoreException(ErrorType.CONFLICT, "포인트가 부족합니다.");
	}
	
	public boolean canPay(Money amount) {
		return getBalance().isGreaterThanOrEqual(amount);
	}
	
	public Money getBalance() {
		return Money.of(balance);
	}

	public static PointEntity createInitial(String userId) {
		return new PointEntity(userId, Money.ZERO());
	}
	
	@Override
	protected void guard() {
		if (userId == null || userId.isBlank()) {
			throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
		}
		if (balance == null) {
			throw new CoreException(ErrorType.BAD_REQUEST, "잔액은 필수입니다.");
		}
		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new CoreException(ErrorType.BAD_REQUEST, "잔액은 0 이상이어야 합니다.");
		}
	}
}
