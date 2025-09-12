package com.loopers.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final double CARRY_OVER_RATIO = 0.1; // 10%만 이전
    private static final int TOP_N = 100; // 상위 100개만 이전

    /**
     * 매일 23:50에 실행
     * 오늘 랭킹 일부를 내일로 이전
     */
    @Scheduled(cron = "0 50 23 * * *")
    public void carryOverScores() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        String todayKey = generateKey(today);
        String tomorrowKey = generateKey(tomorrow);

        log.info("랭킹 캐리오버 시작 - {} -> {}", todayKey, tomorrowKey);

        try {
            // 오늘의 상위 N개 조회 (점수 포함)
            Set<ZSetOperations.TypedTuple<String>> topProducts =
                redisTemplate.opsForZSet().reverseRangeWithScores(todayKey, 0, TOP_N - 1);

            if (topProducts == null || topProducts.isEmpty()) {
                log.info("캐리오버할 랭킹 데이터 없음");
                return;
            }

            // 내일 키에 감소된 점수로 추가
            for (ZSetOperations.TypedTuple<String> tuple : topProducts) {
                String productId = tuple.getValue();
                Double score = tuple.getScore();

                if (productId != null && score != null) {
                    // 10%만 이전
                    double carryOverScore = score * CARRY_OVER_RATIO;
                    redisTemplate.opsForZSet().incrementScore(
                        tomorrowKey,
                        productId,
                        carryOverScore
                    );
                }
            }

            // TTL 설정 (2일)
            redisTemplate.expire(tomorrowKey, java.time.Duration.ofDays(2));

            log.info("랭킹 캐리오버 완료 - {} 개 상품", topProducts.size());

        } catch (Exception e) {
            log.error("랭킹 캐리오버 실패", e);
        }
    }

    private String generateKey(LocalDate date) {
        return String.format("ranking:all:%s", date.format(DATE_FORMAT));
    }
}
