package com.loopers.domain.point;

import com.loopers.domain.common.Money;
import com.loopers.domain.point.vo.Charge;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    
    public Point init(String userId) {
        return Point.createInitial(userId);
    }
    
    public PointHistory charge(Point point, Charge chargeAmount) {
        return point.charge(chargeAmount);
    }
    
    public PointHistory use(Point point, Money amount, Long orderId) {
        return point.use(amount, orderId);
    }
    
    /**
     * 포인트 사용
     */
    @Transactional
    public void usePoint(String userId, Money amount, Long orderId) {
        // 1. 포인트 조회 (비관적 락 사용)
        Point point = pointRepository.findByUserIdWithLock(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "포인트 정보를 찾을 수 없습니다. userId: " + userId));
        
        // 2. 잔액 확인
        if (point.getBalance().compareTo(amount) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                String.format("포인트가 부족합니다. 필요: %s, 현재: %s", 
                    amount, point.getBalance()));
        }
        
        // 3. 포인트 차감
        PointHistory history = use(point, amount, orderId);
        
        // 4. 저장
        pointRepository.save(point);
        pointHistoryRepository.save(history);
        
        log.info("포인트 사용 완료 - userId: {}, amount: {}, orderId: {}", 
            userId, amount, orderId);
    }
    
    /**
     * 포인트 환불
     */
    @Transactional
    public void refundPoint(String userId, Money amount, Long orderId) {
        // 1. 포인트 조회
        Point point = pointRepository.findByUserIdWithLock(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, 
                "포인트 정보를 찾을 수 없습니다. userId: " + userId));
        
        // 2. 포인트 환불
        PointHistory history = point.refund(amount, orderId);
        
        // 3. 저장
        pointRepository.save(point);
        pointHistoryRepository.save(history);
        
        log.info("포인트 환불 완료 - userId: {}, amount: {}, orderId: {}", 
            userId, amount, orderId);
    }
}
