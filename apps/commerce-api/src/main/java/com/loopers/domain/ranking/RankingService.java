package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 랭킹 조회
     */
    public List<RankingInfo> getRankings(RankingCommand.GetList command) {
        String key = generateKey(command.date());

        int start = command.page() * command.size();
        int end = start + command.size() - 1;

        Set<ZSetOperations.TypedTuple<String>> rankings =
            redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);

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

    /**
     * 특정 상품 순위 조회
     */
    public Long getProductRank(Long productId, LocalDate date) {
        String key = generateKey(date);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());

        return rank != null ? rank + 1 : null;
    }

    private String generateKey(LocalDate date) {
        return String.format("ranking:all:%s", date.format(DATE_FORMAT));
    }
}
