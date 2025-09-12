package com.loopers.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RankingRedisRepositoryTest {

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RankingRedisRepository rankingRedisRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private LocalDate testDate;
    private String testKey;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();
        testKey = "ranking:all:" + testDate.format(DATE_FORMAT);
        redisTemplate.delete(testKey);
    }

    @Test
    @DisplayName("단일 점수를 증가시킨다")
    void incrementsScore_whenCalled() {
        // Given
        Long productId = 100L;
        double score = 10.5;

        // When
        rankingRedisRepository.incrementScore(productId, score, testDate);

        // Then
        Double savedScore = redisTemplate.opsForZSet().score(testKey, "100");
        assertThat(savedScore).isEqualTo(10.5);
    }

    @Test
    @DisplayName("동일 상품의 점수를 누적한다")
    void accumulatesScore_whenSameProductScored() {
        // Given
        Long productId = 100L;

        // When
        rankingRedisRepository.incrementScore(productId, 10.0, testDate);
        rankingRedisRepository.incrementScore(productId, 5.0, testDate);

        // Then
        Double savedScore = redisTemplate.opsForZSet().score(testKey, "100");
        assertThat(savedScore).isEqualTo(15.0);
    }

    @Test
    @DisplayName("배치로 여러 점수를 한 번에 업데이트한다")
    void incrementsScoresBatch_whenMultipleScoresProvided() {
        // Given
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1L, 100.0);
        scores.put(2L, 200.0);
        scores.put(3L, 150.0);

        // When
        rankingRedisRepository.incrementScoresBatch(scores, testDate);

        // Then
        Set<String> top3 = redisTemplate.opsForZSet().reverseRange(testKey, 0, 2);
        assertThat(top3)
            .containsExactly("2", "3", "1");  // 점수 높은 순
    }

    @Test
    @DisplayName("TTL이 설정된다")
    void setsTTL_whenKeyCreated() {
        // Given
        Long productId = 100L;

        // When
        rankingRedisRepository.incrementScore(productId, 10.0, testDate);

        // Then
        Long ttl = redisTemplate.getExpire(testKey);
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(172800);  // 2일 = 172800초
    }
}
