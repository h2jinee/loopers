package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
	PointEntity save(PointEntity pointEntity);
	Optional<PointEntity> findByUserId(String userId);
	void clear();
}
