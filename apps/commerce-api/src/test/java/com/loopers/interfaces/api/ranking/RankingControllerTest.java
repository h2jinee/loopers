package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingCriteria;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
class RankingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private RankingFacade rankingFacade;

    @DisplayName("GET /api/v1/rankings")
    @Nested
    class GetRankings {
        private final String ENDPOINT = "/api/v1/rankings";
        
        @DisplayName("일간 랭킹을 정상적으로 조회한다")
        @Test
        void returnsRankings_whenDailyPeriodRequested() throws Exception {
            // arrange
            given(rankingFacade.getRankingsWithProducts(any(RankingCriteria.class)))
                .willReturn(createMockRankings());
            
            // act & assert
            mockMvc.perform(get(ENDPOINT)
                    .param("period", "daily")
                    .param("date", "20250918")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
        }
        
        @DisplayName("주간 랭킹을 정상적으로 조회한다")
        @Test
        void returnsRankings_whenWeeklyPeriodRequested() throws Exception {
            // arrange
            given(rankingFacade.getRankingsWithProducts(any(RankingCriteria.class)))
                .willReturn(createMockRankings());
            
            // act & assert
            mockMvc.perform(get(ENDPOINT)
                    .param("period", "weekly")
                    .param("date", "20250918")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
        }
        
        @DisplayName("월간 랭킹을 정상적으로 조회한다")
        @Test
        void returnsRankings_whenMonthlyPeriodRequested() throws Exception {
            // arrange
            given(rankingFacade.getRankingsWithProducts(any(RankingCriteria.class)))
                .willReturn(createMockRankings());
            
            // act & assert
            mockMvc.perform(get(ENDPOINT)
                    .param("period", "monthly")
                    .param("date", "20250918")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"));
        }
        
        @DisplayName("페이징 파라미터가 정상 동작한다")
        @Test
        void returnsPaginatedResults_whenPageParametersProvided() throws Exception {
            // arrange
            List<RankingResult> paginatedResults = createMockRankings().subList(0, 2);
            given(rankingFacade.getRankingsWithProducts(any(RankingCriteria.class)))
                .willReturn(paginatedResults);
            
            // act & assert
            mockMvc.perform(get(ENDPOINT)
                    .param("period", "daily")
                    .param("date", "20250918")
                    .param("page", "1")
                    .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        }
        
        @DisplayName("데이터가 없으면 빈 리스트를 반환한다")
        @Test
        void returnsEmptyList_whenNoDataExists() throws Exception {
            // arrange
            given(rankingFacade.getRankingsWithProducts(any(RankingCriteria.class)))
                .willReturn(Collections.emptyList());
            
            // act & assert
            mockMvc.perform(get(ENDPOINT)
                    .param("period", "daily")
                    .param("date", "20250918"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
        }
        
        private List<RankingResult> createMockRankings() {
            // 테스트용 Mock 데이터 생성
            return List.of(
                RankingResult.builder()
                    .rank(1)
                    .productId(1L)
                    .productName("상품1")
                    .price(10000L)
                    .brandId(1L)
                    .score(100.0)
                    .build(),
                RankingResult.builder()
                    .rank(2)
                    .productId(2L)
                    .productName("상품2")
                    .price(20000L)
                    .brandId(2L)
                    .score(90.0)
                    .build(),
                RankingResult.builder()
                    .rank(3)
                    .productId(3L)
                    .productName("상품3")
                    .price(30000L)
                    .brandId(3L)
                    .score(80.0)
                    .build()
            );
        }
    }
}
