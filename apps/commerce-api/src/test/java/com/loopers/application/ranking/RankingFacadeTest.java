package com.loopers.application.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RankingFacadeTest {

    @Autowired
    private RankingFacade rankingFacade;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        String key = "ranking:all:" + LocalDate.now().format(DATE_FORMAT);
        redisTemplate.delete(key);
    }

    @Test
    @DisplayName("랭킹이 없을 때 빈 리스트를 반환한다")
    void returnsEmptyList_whenNoRankingExists() {
        // Given
        RankingCriteria criteria = new RankingCriteria(LocalDate.now(), 0, 20);

        // When
        var rankings = rankingFacade.getRankingsWithProducts(criteria);

        // Then
        assertThat(rankings).isEmpty();
    }

    @Test
    @DisplayName("랭킹 데이터가 있을 때 정렬된 순서로 반환한다")
    void returnsRankingsInOrder_whenRankingExists() {
        // Given
        LocalDate today = LocalDate.now();
        String key = "ranking:all:" + today.format(DATE_FORMAT);

        // 테스트 데이터 설정
        redisTemplate.opsForZSet().add(key, "1", 100.0);
        redisTemplate.opsForZSet().add(key, "2", 200.0);
        redisTemplate.opsForZSet().add(key, "3", 150.0);

        RankingCriteria criteria = new RankingCriteria(today, 0, 20);

        // When
        var rankings = rankingFacade.getRankingsWithProducts(criteria);

        // Then
        assertThat(rankings)
            .hasSize(3)
            .extracting("rank")
            .containsExactly(1, 2, 3);

        assertThat(rankings)
            .extracting("productId")
            .containsExactly(2L, 3L, 1L);  // 점수 높은 순
    }

    @Test
    @DisplayName("페이징이 정상적으로 동작한다")
    void returnsPaginatedResults_whenPageRequested() {
        // Given
        LocalDate today = LocalDate.now();
        String key = "ranking:all:" + today.format(DATE_FORMAT);

        // 10개 상품 추가
        for (int i = 1; i <= 10; i++) {
            redisTemplate.opsForZSet().add(key, String.valueOf(i), i * 10.0);
        }

        RankingCriteria criteria = new RankingCriteria(today, 1, 3);  // 2페이지, 3개씩

        // When
        var rankings = rankingFacade.getRankingsWithProducts(criteria);

        // Then
        assertThat(rankings)
            .hasSize(3)
            .extracting("rank")
            .containsExactly(4, 5, 6);  // 4등부터 6등까지
    }
}
