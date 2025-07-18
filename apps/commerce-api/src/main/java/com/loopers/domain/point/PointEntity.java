package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "point")
public class PointEntity extends BaseEntity {
	@Id
	private String userId;

	private Long point;

	public PointEntity(String userId, Long point) {
		if (point == null || point <= 0) {
			throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 0보다 큰 값이어야 합니다.");
		}
		this.userId = userId;
		this.point = point;
	}
}
