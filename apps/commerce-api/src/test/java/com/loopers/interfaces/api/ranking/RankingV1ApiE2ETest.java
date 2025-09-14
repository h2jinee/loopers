package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RankingV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        String todayKey = "ranking:all:" + LocalDate.now().format(DATE_FORMAT);
        redisTemplate.delete(todayKey);

        // 테스트 데이터 설정
        redisTemplate.opsForZSet().add(todayKey, "1", 100.0);
        redisTemplate.opsForZSet().add(todayKey, "2", 200.0);
        redisTemplate.opsForZSet().add(todayKey, "3", 150.0);
    }

    @DisplayName("GET /api/v1/rankings")
    @Nested
    class getRankings {
        private final String ENDPOINT = "/api/v1/rankings";

        @DisplayName("특정 날짜를 지정하면 해당 날짜 랭킹을 반환한다")
        @Test
        void returnsSpecificDateRanking_whenDateProvided() {
            // arrange
            LocalDate yesterday = LocalDate.now().minusDays(1);
            String yesterdayKey = "ranking:all:" + yesterday.format(DATE_FORMAT);
            redisTemplate.opsForZSet().add(yesterdayKey, "10", 500.0);

            String url = ENDPOINT + "?date=" + yesterday.format(DATE_FORMAT);

            // act
            ParameterizedTypeReference<ApiResponse<List<RankingDto.V1.GetList.Response>>> responseType =
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<RankingDto.V1.GetList.Response>>> response =
                testRestTemplate.exchange(url, HttpMethod.GET, null, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<RankingDto.V1.GetList.Response> rankings = response.getBody().data();
            assertThat(rankings)
                .hasSize(1)
                .first()
                .satisfies(ranking -> {
                    assertThat(ranking.rank()).isEqualTo(1);
                    assertThat(ranking.productId()).isEqualTo(10L);
                    assertThat(ranking.score()).isEqualTo(500.0);
                });
        }

        @DisplayName("페이징 파라미터가 정상 동작한다")
        @Test
        void returnsPaginatedResults_whenPageParametersProvided() {
            // arrange
            String url = ENDPOINT + "?page=0&size=2";

            // act
            ParameterizedTypeReference<ApiResponse<List<RankingDto.V1.GetList.Response>>> responseType =
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<RankingDto.V1.GetList.Response>>> response =
                testRestTemplate.exchange(url, HttpMethod.GET, null, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<RankingDto.V1.GetList.Response> rankings = response.getBody().data();
            assertThat(rankings)
                .hasSize(2)
                .extracting("rank")
                .containsExactly(1, 2);
        }

        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        @Test
        void returnsEmptyList_whenNoDataExists() {
            // arrange
            LocalDate future = LocalDate.now().plusDays(100);
            String url = ENDPOINT + "?date=" + future.format(DATE_FORMAT);

            // act
            ParameterizedTypeReference<ApiResponse<List<RankingDto.V1.GetList.Response>>> responseType =
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<RankingDto.V1.GetList.Response>>> response =
                testRestTemplate.exchange(url, HttpMethod.GET, null, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<RankingDto.V1.GetList.Response> rankings = response.getBody().data();
            assertThat(rankings).isEmpty();
        }
    }
}
