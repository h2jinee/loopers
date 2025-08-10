package com.loopers.application.point;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointFacade {
    
    private final PointService pointService;
    
    public PointResult.ChargeResult chargeUserPoint(PointCriteria.Charge criteria) {
        PointCommand.Charge command = criteria.toCommand();
        PointEntity charged = pointService.charge(command);
        
        PointInfo.ChargeResult domainInfo = PointInfo.ChargeResult.from(charged);
        return PointResult.ChargeResult.from(domainInfo);
    }
    
    public PointResult.Detail getUserPoint(PointCriteria.GetDetail criteria) {
        PointCommand.GetOne command = criteria.toCommand();
        PointEntity point = pointService.getPointEntity(command);
        
        PointInfo.Detail domainInfo = PointInfo.Detail.from(point);
        return PointResult.Detail.from(domainInfo);
    }
}