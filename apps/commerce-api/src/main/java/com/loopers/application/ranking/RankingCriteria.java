package com.loopers.application.ranking;

import java.time.LocalDate;

public record RankingCriteria(
    LocalDate date,
    int page,
    int size
) {
    public RankingCriteria {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}
