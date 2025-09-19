package com.loopers.domain.ranking;

import java.time.LocalDate;

public class RankingCommand {

    public record GetList(
        PeriodType period,
        LocalDate date,
        int page,
        int size
    ) {
        private static final int DEFAULT_PAGE = 0;
        private static final int DEFAULT_SIZE = 20;
        private static final int MAX_SIZE = 100;

        public GetList {
            if (page < 0) page = DEFAULT_PAGE;
            if (size <= 0) size = DEFAULT_SIZE;
            if (size > MAX_SIZE) size = MAX_SIZE;
        }
    }
}
