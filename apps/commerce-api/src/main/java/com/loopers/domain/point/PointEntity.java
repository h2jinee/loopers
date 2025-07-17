package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;

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
		this.userId = userId;
		this.point = point;
	}
}
