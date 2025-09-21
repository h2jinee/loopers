package com.loopers.application.ranking;

import com.loopers.domain.ranking.PeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @DisplayName("랭킹 조회 기능")
    @Nested
    class GetRankingsWithProducts {
        
        @DisplayName("랭킹이 없을 때 빈 리스트를 반환한다")
        @Test
        void returnsEmptyList_whenNoRankingExists() {
            // arrange
            RankingCriteria criteria = new RankingCriteria(
                PeriodType.DAILY, 
                LocalDate.now(), 
                0, 
                20
            );

            // act
            var rankings = rankingFacade.getRankingsWithProducts(criteria);

            // assert
            assertThat(rankings).isEmpty();
        }

        @DisplayName("랭킹 데이터가 있을 때 정렬된 순서로 반환한다")
        @Test
        void returnsRankingsInOrder_whenRankingExists() {
            // arrange
            LocalDate today = LocalDate.now();
            String key = "ranking:all:" + today.format(DATE_FORMAT);

            // 테스트 데이터 설정
            redisTemplate.opsForZSet().add(key, "1", 100.0);
            redisTemplate.opsForZSet().add(key, "2", 200.0);
            redisTemplate.opsForZSet().add(key, "3", 150.0);

            RankingCriteria criteria = new RankingCriteria(
                PeriodType.DAILY,
                today,
                0,
                20
            );

            // act
            var rankings = rankingFacade.getRankingsWithProducts(criteria);

            // assert
            assertThat(rankings)
                .hasSize(3)
                .extracting("rank")
                .containsExactly(1, 2, 3);

            assertThat(rankings)
                .extracting("productId")
                .containsExactly(2L, 3L, 1L);  // 점수 높은 순
        }

        @DisplayName("페이징이 정상적으로 동작한다")
        @Test
        void returnsPaginatedResults_whenPageRequested() {
            // arrange
            LocalDate today = LocalDate.now();
            String key = "ranking:all:" + today.format(DATE_FORMAT);

            // 10개 상품 추가
            for (int i = 1; i <= 10; i++) {
                redisTemplate.opsForZSet().add(key, String.valueOf(i), i * 10.0);
            }

            RankingCriteria criteria = new RankingCriteria(
                PeriodType.DAILY,
                today,
                1,
                3
            );  // 2페이지, 3개씩

            // act
            var rankings = rankingFacade.getRankingsWithProducts(criteria);

            // assert
            assertThat(rankings)
                .hasSize(3)
                .extracting("rank")
                .containsExactly(4, 5, 6);  // 4등부터 6등까지
        }
        
        @DisplayName("점수가 높은 순서대로 순위가 매겨진다")
        @Test
        void assignsRankByScoreDescending_whenMultipleProductsExist() {
            // arrange
            LocalDate today = LocalDate.now();
            String key = "ranking:all:" + today.format(DATE_FORMAT);

            redisTemplate.opsForZSet().add(key, "1", 50.0);
            redisTemplate.opsForZSet().add(key, "2", 300.0);  // 최고 점수
            redisTemplate.opsForZSet().add(key, "3", 150.0);
            redisTemplate.opsForZSet().add(key, "4", 75.0);

            RankingCriteria criteria = new RankingCriteria(
                PeriodType.DAILY,
                today,
                0,
                20
            );

            // act
            var rankings = rankingFacade.getRankingsWithProducts(criteria);

            // assert
            assertThat(rankings).hasSize(4);
            assertThat(rankings.get(0))
                .satisfies(ranking -> {
                    assertThat(ranking.rank()).isEqualTo(1);
                    assertThat(ranking.productId()).isEqualTo(2L);
                    assertThat(ranking.score()).isEqualTo(300.0);
                });
            assertThat(rankings.get(1))
                .satisfies(ranking -> {
                    assertThat(ranking.rank()).isEqualTo(2);
                    assertThat(ranking.productId()).isEqualTo(3L);
                    assertThat(ranking.score()).isEqualTo(150.0);
                });
        }
        
        @DisplayName("페이징 크기만큼만 결과를 반환한다")
        @Test
        void returnsLimitedResults_whenSizeParameterProvided() {
            // arrange
            LocalDate today = LocalDate.now();
            String key = "ranking:all:" + today.format(DATE_FORMAT);

            // 20개 상품 추가
            for (int i = 1; i <= 20; i++) {
                redisTemplate.opsForZSet().add(key, String.valueOf(i), i * 10.0);
            }

            RankingCriteria criteria = new RankingCriteria(
                PeriodType.DAILY,
                today,
                0,
                5
            );  // 5개만 요청

            // act
            var rankings = rankingFacade.getRankingsWithProducts(criteria);

            // assert
            assertThat(rankings).hasSize(5);
            assertThat(rankings)
                .extracting("rank")
                .containsExactly(1, 2, 3, 4, 5);
        }
    }
}
