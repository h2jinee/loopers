package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    /**
     * 랭킹 조회
     */
    public List<RankingInfo> getRankings(RankingCommand.GetList command) {
        return switch (command.period()) {
            case DAILY -> rankingRepository.getRankings(command.date(), command.page(), command.size());
            case WEEKLY -> rankingRepository.getWeeklyRankings(command.date(), command.page(), command.size());
            case MONTHLY -> rankingRepository.getMonthlyRankings(command.date(), command.page(), command.size());
        };
    }

    /**
     * 특정 상품 순위 조회
     */
    public Long getProductRank(Long productId, LocalDate date) {
        return rankingRepository.getProductRank(productId, date);
    }
}
