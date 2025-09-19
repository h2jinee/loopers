package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface RankingRepository {

    List<RankingInfo> getRankings(LocalDate date, int page, int size);

    Long getProductRank(Long productId, LocalDate date);

    List<RankingInfo> getWeeklyRankings(LocalDate date, int page, int size);

    List<RankingInfo> getMonthlyRankings(LocalDate date, int page, int size);
}
