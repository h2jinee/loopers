package com.loopers.application.point;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.vo.ChargePoint;

public class PointCriteria {
    
    public record Charge(
        String userId,
        Long amount
    ) {
        public PointCommand.Charge toCommand() {
            return new PointCommand.Charge(
                userId,
                new ChargePoint(amount)
            );
        }
    }
    
    public record GetDetail(
        String userId
    ) {
        public PointCommand.GetOne toCommand() {
            return new PointCommand.GetOne(userId);
        }
    }
}