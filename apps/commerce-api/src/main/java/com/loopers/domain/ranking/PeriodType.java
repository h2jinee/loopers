package com.loopers.domain.ranking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PeriodType {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    public static PeriodType from(String value) {
        for (PeriodType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 기간 타입: " + value);
    }
}
