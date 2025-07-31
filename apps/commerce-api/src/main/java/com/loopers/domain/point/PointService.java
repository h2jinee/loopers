package com.loopers.domain.point;

import org.springframework.stereotype.Service;

import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final PointRepository pointRepository;
	private final UserRepository userRepository;

	public PointEntity charge(PointCommand.Charge command) {
        // 유저 존재 여부 확인
        if (!userRepository.existsByUserId(command.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        // 기존 포인트 조회 또는 생성
        PointEntity point = pointRepository.findById(command.userId())
            .orElseGet(() -> PointEntity.createInitial(command.userId()));

        // 충전
        point.charge(command.amount());

        return pointRepository.save(point);
    }

	public void initializeUserPoint(String userId) {
		// 이미 포인트가 있는지 확인
		if (pointRepository.findById(userId).isPresent()) {
			return;
		}

		// 초기 포인트 생성 (0포인트)
		PointEntity initialPoint = PointEntity.createInitial(userId);
		pointRepository.save(initialPoint);
	}

	public Long getUserPoint(String userId) {
		return pointRepository.findById(userId)
			.map(PointEntity::getPoint)
			.orElse(0L);
	}
}
