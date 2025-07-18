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
		UserEntity user = userService.getUserInfo(pointEntity.getUserId());
		if (user == null) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}
		return  pointRepository.save(pointEntity);
	}

	public Long getUserPoint(String userId) {
		return pointRepository.findByUserId(userId)
			.map(PointEntity::getPoint)
			.orElse(null);
	}
}
