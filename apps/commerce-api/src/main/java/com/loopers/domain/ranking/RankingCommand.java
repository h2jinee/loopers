package com.loopers.domain.ranking;

import java.time.LocalDate;

public class RankingCommand {

    public record GetList(
        LocalDate date,
        int page,
        int size
    ) {
        public GetList {
            if (date == null) date = LocalDate.now();
            if (page < 0) page = 0;
            if (size <= 0) size = 20;
            if (size > 100) size = 100;
        }
    }
}
