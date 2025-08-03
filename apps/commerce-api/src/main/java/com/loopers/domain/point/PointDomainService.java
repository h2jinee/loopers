package com.loopers.domain.point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointDomainService {
	private final PointRepository pointRepository;
	private final PointHistoryRepository pointHistoryRepository;
	private final UserRepository userRepository;

	@Transactional
	public PointEntity charge(PointCommand.Charge command) {
		// 유저 존재 여부 확인
		if (!userRepository.existsByUserId(command.userId())) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}

		// 기존 포인트 조회 또는 생성
		PointEntity point = pointRepository.findById(command.userId())
			.orElseGet(() -> PointEntity.createInitial(command.userId()));

		// 충전 및 히스토리 생성
		PointHistoryEntity history = point.charge(command.amount());
		
		// 포인트 저장
		point = pointRepository.save(point);
		
		// 히스토리 저장
		pointHistoryRepository.save(history);

		return point;
	}

	@Transactional
	public void initializeUserPoint(PointCommand.Initialize command) {
		// 이미 포인트가 있는지 확인
		if (pointRepository.findById(command.userId()).isPresent()) {
			return;
		}

		// 초기 포인트 생성 (0포인트)
		PointEntity initialPoint = PointEntity.createInitial(command.userId());
		pointRepository.save(initialPoint);
	}
	
	public PointEntity getPointEntity(PointCommand.GetOne command) {
		return getPointEntityByUserId(command.userId());
	}
	
	private PointEntity getPointEntityByUserId(String userId) {
		return pointRepository.findById(userId)
			.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));
	}
	
	@Transactional
	public void usePoint(PointCommand.Use command) {
		PointEntity point = getPointEntityByUserId(command.userId());
		
		// 포인트 사용 및 히스토리 생성
		PointHistoryEntity history = point.use(command.amount(), command.orderId());
		
		// 포인트 저장
		pointRepository.save(point);
		
		// 히스토리 저장
		pointHistoryRepository.save(history);
	}
}
