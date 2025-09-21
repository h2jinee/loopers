package com.loopers.batch.processor;

import com.loopers.batch.reader.dto.ProductMetricsAggregation;
import com.loopers.domain.ranking.WeeklyRanking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class WeeklyRankingProcessorTest {
    
    private WeeklyRankingProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = new WeeklyRankingProcessor();
        
        // JobParameters 주입
        ReflectionTestUtils.setField(processor, "startDate", "2025-09-15");
        ReflectionTestUtils.setField(processor, "endDate", "2025-09-21");
        
        // 가중치 설정
        ReflectionTestUtils.setField(processor, "likeWeight", 1.0);
        ReflectionTestUtils.setField(processor, "orderWeight", 100.0);
        ReflectionTestUtils.setField(processor, "salesWeight", 10.0);
    }
    
    @DisplayName("WeeklyRankingProcessor 처리")
    @Nested
    class Process {
        
        @DisplayName("점수 계산이 정확하게 처리되어야 한다")
        @Test
        void calculatesScoreCorrectly_whenMetricsProvided() throws Exception {
            // arrange
            ProductMetricsAggregation metrics = new ProductMetricsAggregation(
                1L,    // productId
                5L,    // totalLikes
                10L,   // totalOrders
                20L    // totalSales
            );
            
            // act
            WeeklyRanking result = processor.process(metrics);
            
            // assert
            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(1L);
            assertThat(result.getScore()).isEqualTo(1205.0); // 5*1 + 10*100 + 20*10
            assertThat(result.getLikeCount()).isEqualTo(5L);
            assertThat(result.getOrderCount()).isEqualTo(10L);
            assertThat(result.getSalesQuantity()).isEqualTo(20L);
        }
        
        @DisplayName("날짜가 올바르게 설정되어야 한다")
        @Test
        void setsDateRangeCorrectly_whenProcessed() throws Exception {
            // arrange
            ProductMetricsAggregation metrics = new ProductMetricsAggregation(
                1L, 0L, 0L, 0L
            );
            
            // act
            WeeklyRanking result = processor.process(metrics);
            
            // assert
            assertThat(result).isNotNull();
            assertThat(result.getPeriodStart()).isEqualTo(LocalDate.parse("2025-09-15"));
            assertThat(result.getPeriodEnd()).isEqualTo(LocalDate.parse("2025-09-21"));
        }
        
        @DisplayName("모든 메트릭이 0일 때도 정상 처리되어야 한다")
        @Test
        void returnsZeroScore_whenAllMetricsAreZero() throws Exception {
            // arrange
            ProductMetricsAggregation metrics = new ProductMetricsAggregation(
                100L, 0L, 0L, 0L
            );
            
            // act
            WeeklyRanking result = processor.process(metrics);
            
            // assert
            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(100L);
            assertThat(result.getScore()).isEqualTo(0.0);
            assertThat(result.getLikeCount()).isZero();
            assertThat(result.getOrderCount()).isZero();
            assertThat(result.getSalesQuantity()).isZero();
        }
    }
}
