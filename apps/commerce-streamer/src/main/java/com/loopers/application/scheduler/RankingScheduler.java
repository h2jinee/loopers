package com.loopers.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
	private static final double CARRY_OVER_RATIO = 0.01;
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
			Long existingSize = redisTemplate.opsForZSet().size(tomorrowKey);
			if (existingSize != null && existingSize > 0) {
				log.warn("내일 키에 이미 데이터 존재 ({}개), 캐리오버 스킵", existingSize);
				return;
			}

			Long result = redisTemplate.opsForZSet().unionAndStore(
				todayKey,
				Collections.emptyList(),
				tomorrowKey,
				Aggregate.SUM,
				Weights.of(CARRY_OVER_RATIO)
			);

			if (result == null || result == 0) {
				log.info("캐리오버할 데이터 없음");
				return;
			}

			// 상위 100개만 유지
			redisTemplate.opsForZSet().removeRange(tomorrowKey, TOP_N, -1);

			// TTL 설정
			redisTemplate.expire(tomorrowKey, Duration.ofDays(2));

			log.info("랭킹 캐리오버 완료 - {} 개 상품", Math.min(result, TOP_N));

		} catch (Exception e) {
			log.error("랭킹 캐리오버 실패", e);
		}
	}

    private String generateKey(LocalDate date) {
        return String.format("ranking:all:%s", date.format(DATE_FORMAT));
    }
}
