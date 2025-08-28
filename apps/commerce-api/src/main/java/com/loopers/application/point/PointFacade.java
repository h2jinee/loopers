package com.loopers.application.point;

import com.loopers.domain.common.Money;
import com.loopers.domain.point.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointFacade {
    
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointService pointService;
    
    /**
     * 포인트 충전
     */
    @Transactional
    public PointResult.Charged charge(PointCriteria.Charge criteria) {
        PointCommand.Charge command = criteria.toCommand();
        
        // 1. 기존 포인트 조회 또는 생성 (비관적 락 사용)
        Point point = pointRepository.findByUserIdWithLock(command.userId())
            .orElseGet(() -> pointService.init(command.userId()));
        
        // 2. 충전
        PointHistory history = pointService.charge(point, command.amount());
        
        // 3. 저장
        Point savedPoint = pointRepository.save(point);
        pointHistoryRepository.save(history);
        
        // 4. 결과 반환
        PointInfo.Charged domainInfo = PointInfo.Charged.from(savedPoint);
        return PointResult.Charged.from(domainInfo);
    }
    
    /**
     * 포인트 사용
     */
    @Transactional
    public void use(PointCommand.Use command) {
        // 1. 포인트 조회 (비관적 락 사용)
        Point point = pointRepository.findByUserIdWithLock(command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "포인트 정보를 찾을 수 없습니다. userId: " + command.userId()));
        
        // 2. 도메인 서비스로 사용 처리 (순수 비즈니스 로직)
        PointHistory history = pointService.use(point, command.amount(), command.orderId());
        
        // 3. 저장
        pointRepository.save(point);
        pointHistoryRepository.save(history);
        
        log.info("포인트 사용 완료 - userId: {}, amount: {}, orderId: {}", 
            command.userId(), command.amount(), command.orderId());
    }
    
    /**
     * 포인트 조회
     */
    public PointResult.Detail getUserPoint(PointCriteria.GetDetail criteria) {
        PointCommand.GetOne command = criteria.toCommand();
        
        Point point = pointRepository.findByUserId(command.userId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "포인트 정보를 찾을 수 없습니다. userId: " + command.userId()));
        
        PointInfo.Detail domainInfo = PointInfo.Detail.from(point);
        return PointResult.Detail.from(domainInfo);
    }
    
    /**
     * 포인트 초기화
     * - 사용자 등록 시 호출
     */
    @Transactional
    public void initializeUserPoint(String userId) {
        // 이미 포인트가 있는지 확인
        if (pointRepository.findByUserId(userId).isPresent()) {
            log.info("이미 포인트가 존재합니다. userId: {}", userId);
            return;
        }
        
        // 초기 포인트 생성
        Point initialPoint = pointService.init(userId);
        pointRepository.save(initialPoint);
        
        log.info("포인트 초기화 완료 - userId: {}", userId);
    }
}
