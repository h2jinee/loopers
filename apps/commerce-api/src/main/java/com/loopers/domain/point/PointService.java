package com.loopers.domain.point;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final PointRepository pointRepository;

	public PointEntity save(PointEntity pointEntity) {
		return  pointRepository.save(pointEntity);
	}

	public Long getUserPoint(String userId) {
		return pointRepository.findByUserId(userId)
			.map(PointEntity::getPoint)
			.orElse(null);
	}
}
