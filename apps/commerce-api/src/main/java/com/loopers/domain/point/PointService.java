package com.loopers.domain.point;

import com.loopers.domain.common.Money;
import com.loopers.domain.point.vo.Charge;
import org.springframework.stereotype.Service;

@Service
public class PointService {
    
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
     * 포인트 사용 가능 여부 확인
     */
    public boolean canUse(Point point, Money amount) {
        return point != null && point.canPay(amount);
    }
}
