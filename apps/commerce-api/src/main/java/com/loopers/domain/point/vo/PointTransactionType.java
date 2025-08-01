package com.loopers.domain.point.vo;

public enum PointTransactionType {
    CHARGE("충전"),
    USE("사용");
    
    private final String description;
    
    PointTransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
