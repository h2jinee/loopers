package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingInfo;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.MonthlyRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final WeeklyRankingJpaRepository weeklyRepository;
    private final MonthlyRankingJpaRepository monthlyRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public List<RankingInfo> getRankings(LocalDate date, int page, int size) {
        String key = generateKey(date);

        int start = page * size;
        int end = start + size - 1;

        Set<ZSetOperations.TypedTuple<String>> rankings = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);

        if (rankings == null || rankings.isEmpty()) {
            return Collections.emptyList();
        }

        List<RankingInfo> result = new ArrayList<>();
        int rank = start + 1;

        for (ZSetOperations.TypedTuple<String> tuple : rankings) {
            String value = tuple.getValue();
            Double score = tuple.getScore();

            if (value != null && score != null) {
                try {
                    Long productId = Long.parseLong(value);
                    result.add(RankingInfo.of(rank++, productId, score));
                } catch (NumberFormatException e) {
                    log.warn("랭킹에 잘못된 상품 ID: {}", value);
                }
            }
        }

        return result;
    }

    @Override
    public Long getProductRank(Long productId, LocalDate date) {
        String key = generateKey(date);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());
        return rank != null ? rank + 1 : null;
    }

    @Override
    public List<RankingInfo> getWeeklyRankings(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<WeeklyRanking> rankings = weeklyRepository.findByPeriodEnd(date, pageable);

        List<RankingInfo> result = new ArrayList<>();
        int rank = page * size + 1;

        for (WeeklyRanking mv : rankings) {
            result.add(RankingInfo.of(rank++, mv.getProductId(), mv.getScore()));
        }

        return result;
    }

    @Override
    public List<RankingInfo> getMonthlyRankings(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<MonthlyRanking> rankings = monthlyRepository.findByPeriodEnd(date, pageable);

        List<RankingInfo> result = new ArrayList<>();
        int rank = page * size + 1;

        for (MonthlyRanking mv : rankings) {
            result.add(RankingInfo.of(rank++, mv.getProductId(), mv.getScore()));
        }

        return result;
    }

    private String generateKey(LocalDate date) {
        return String.format("ranking:all:%s", date.format(DATE_FORMAT));
    }
}
