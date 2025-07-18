package com.loopers.domain.point;

import org.springframework.stereotype.Service;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final PointRepository pointRepository;
	private final UserService userService;

	public PointEntity save(PointEntity pointEntity) {
		// 유저 존재 여부 검증
		UserEntity user = userService.getUserInfo(pointEntity.getUserId());
		if (user == null) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}

		// 충전 금액 검증
		if (pointEntity.getPoint() == null ||  pointEntity.getPoint() <= 0) {
			throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
		}

		// 기존 포인트 조회
		Long currentPoint = getUserPoint(pointEntity.getUserId());

		PointEntity newPointEntity = new  PointEntity(
			pointEntity.getUserId(),
			currentPoint + pointEntity.getPoint()
		);
		return  pointRepository.save(newPointEntity);
	}

	public Long getUserPoint(String userId) {
		return pointRepository.findByUserId(userId)
			.map(PointEntity::getPoint)
			.orElse(null);
	}
}
