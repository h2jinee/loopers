package com.loopers.application.ranking;

import com.loopers.domain.ranking.PeriodType;
import java.time.LocalDate;

public record RankingCriteria(
    PeriodType period,
    LocalDate date,
    int page,
    int size
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public RankingCriteria {
        if (page < 0) page = DEFAULT_PAGE;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
