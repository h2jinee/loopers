package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.vo.ChargePoint;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point")
public class PointEntity extends BaseEntity {
	@Id
	private String userId;
	private Long point;

	public PointEntity(String userId, Long point) {
		this.userId = userId;
		this.point = point;
	}

	public void charge(ChargePoint chargeAmount) {
		this.point += chargeAmount.value();
	}

	public static PointEntity createInitial(String userId) {
		return new PointEntity(userId, 0L);
	}
}
