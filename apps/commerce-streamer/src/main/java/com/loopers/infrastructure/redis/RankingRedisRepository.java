package com.loopers.infrastructure.redis;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RankingRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long TTL_DAYS = 2;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 상품 점수 증가
     */
    public void incrementScore(Long productId, double score, LocalDate date) {
        String key = generateKey(date);
        String member = productId.toString();

        // 기존 점수에 score 더하기 (없으면 0에서 시작)
        redisTemplate.opsForZSet().incrementScore(key, member, score);

        // TTL 설정 (키가 처음 생성될 때만)
        redisTemplate.expire(key, Duration.ofDays(TTL_DAYS));

        log.debug("랭킹 점수 업데이트 - key: {}, productId: {}, score: +{}",
            key, productId, score);
    }

    /**
     * 날짜별 키 생성
     * 형식: ranking:all:20241206
     */
    private String generateKey(LocalDate date) {
        return String.format("ranking:all:%s", date.format(DATE_FORMAT));
    }

    /**
     * Pipeline을 사용한 배치 업데이트
     */
    public void incrementScoresBatch(Map<Long, Double> productScores, LocalDate date) {
        if (productScores.isEmpty()) {
            return;
        }

        String key = generateKey(date);

        // 한 번에 처리
        productScores.forEach((productId, score) -> {
            String member = productId.toString();
            redisTemplate.opsForZSet().incrementScore(key, member, score);
        });

        // TTL 설정
        redisTemplate.expire(key, Duration.ofDays(TTL_DAYS));

        log.debug("배치 랭킹 업데이트 - key: {}, 상품 수: {}", key, productScores.size());
    }
}
