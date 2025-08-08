package com.loopers.domain.point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.point.PointHistoryJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final PointJpaRepository pointJpaRepository;
	private final PointHistoryJpaRepository pointHistoryJpaRepository;
	private final UserJpaRepository userJpaRepository;

	@Transactional
	public PointEntity charge(PointCommand.Charge command) {
		// 1. 사용자 존재 여부 확인
		if (!userJpaRepository.existsByUserId(command.userId())) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}

		// 2. 기존 포인트 조회 또는 초기 생성
		PointEntity point = pointJpaRepository.findById(command.userId())
			.orElseGet(() -> PointEntity.createInitial(command.userId()));

		// 3. 포인트 충전 및 히스토리 생성
		PointHistoryEntity history = point.charge(command.amount());
		
		// 4. 포인트 정보 저장
		point = pointJpaRepository.save(point);
		
		// 5. 포인트 히스토리 저장
		pointHistoryJpaRepository.save(history);

		return point;
	}

	@Transactional
	public void initializeUserPoint(PointCommand.Initialize command) {
		// 1. 이미 포인트가 있는지 확인
		if (pointJpaRepository.findById(command.userId()).isPresent()) {
			return;
		}

		// 2. 새 사용자에 대한 초기 포인트 생성 (0포인트)
		PointEntity initialPoint = PointEntity.createInitial(command.userId());
		pointJpaRepository.save(initialPoint);
	}
	
	public PointEntity getPointEntity(PointCommand.GetOne command) {
		return getPointEntityByUserId(command.userId());
	}
	
	private PointEntity getPointEntityByUserId(String userId) {
		return pointJpaRepository.findById(userId)
			.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));
	}
	
	@Transactional
	public void usePoint(PointCommand.Use command) {
		// 1. 사용자 포인트 정보 조회
		PointEntity point = getPointEntityByUserId(command.userId());
		
		// 2. 포인트 사용 및 히스토리 생성
		PointHistoryEntity history = point.use(command.amount(), command.orderId());
		
		// 3. 업데이트된 포인트 정보 저장
		pointJpaRepository.save(point);
		
		// 4. 포인트 사용 히스토리 저장
		pointHistoryJpaRepository.save(history);
	}
}
