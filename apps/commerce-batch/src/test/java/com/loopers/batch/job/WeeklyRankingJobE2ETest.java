package com.loopers.batch.job;

import com.loopers.batch.processor.WeeklyRankingProcessor;
import com.loopers.batch.reader.ProductMetricsReader;
import com.loopers.batch.writer.WeeklyRankingWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
@Import({
    WeeklyRankingJobConfig.class,
    ProductMetricsReader.class,
    WeeklyRankingProcessor.class,
    WeeklyRankingWriter.class
})
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WeeklyRankingJobE2ETest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    
    @Autowired
    private Job weeklyRankingJob;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void setUp() {
        // JobLauncherTestUtils에 Job 설정
        jobLauncherTestUtils.setJob(weeklyRankingJob);
        
        // 배치 메타 데이터 정리
        jobRepositoryTestUtils.removeJobExecutions();
        
        // 테이블 생성 (테스트용)
        createTablesIfNotExists();
        
        // 기존 데이터 정리
        cleanupData();
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        cleanupData();
    }
    
    private void cleanupData() {
        jdbcTemplate.execute("DELETE FROM mv_product_rank_weekly WHERE 1=1");
        jdbcTemplate.execute("DELETE FROM product_metrics WHERE 1=1");
    }
    
    private void createTablesIfNotExists() {
        // product_metrics 테이블이 없을 때만 생성 (기존 테이블 구조 유지)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS product_metrics (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                product_id BIGINT NOT NULL,
                metric_date DATE NOT NULL,
                like_count BIGINT DEFAULT 0,
                order_count BIGINT DEFAULT 0,
                sales_quantity BIGINT DEFAULT 0,
                updated_at DATETIME(6) NOT NULL
            )
        """);
    }
    
    @DisplayName("주간 랭킹 배치 Job")
    @Nested
    class WeeklyRankingJobTests {
        private final String START_DATE = "2025-09-15";
        private final String END_DATE = "2025-09-21";
        
        @DisplayName("정상적으로 실행되면 완료 상태를 반환한다")
        @Test
        void returnsCompletedStatus_whenJobExecutesSuccessfully() throws Exception {
            // arrange
            insertTestMetricsData();
            JobParameters params = createJobParameters();
            
            // act
            JobExecution execution = jobLauncherTestUtils.launchJob(params);
            
            // assert
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(execution.getAllFailureExceptions()).isEmpty();
        }
        
        @DisplayName("MV 테이블에 랭킹 데이터가 정상적으로 저장된다")
        @Test
        void savesRankingDataToMvTable_whenJobCompletes() throws Exception {
            // arrange
            insertTestMetricsData();
            JobParameters params = createJobParameters();
            
            // act
            JobExecution execution = jobLauncherTestUtils.launchJob(params);
            
            // assert
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly WHERE period_start = ?",
                Integer.class,
                START_DATE
            );
            assertThat(count).isEqualTo(2);  // 2개 상품 (product_id 1, 2)
        }
        
        @DisplayName("데이터가 없을 때도 정상적으로 완료된다")
        @Test
        void returnsCompletedStatus_whenNoDataExists() throws Exception {
            // arrange
            // 데이터 삽입하지 않음
            JobParameters params = createJobParameters();
            
            // act
            JobExecution execution = jobLauncherTestUtils.launchJob(params);
            
            // assert
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly",
                Integer.class
            );
            assertThat(count).isEqualTo(0);
        }
        
        private void insertTestMetricsData() {
            jdbcTemplate.execute("""
                INSERT INTO product_metrics (product_id, metric_date, like_count, order_count, sales_quantity, updated_at)
                VALUES 
                    (1, '2025-09-15', 10, 5, 15, NOW()),
                    (1, '2025-09-16', 5, 3, 8, NOW()),
                    (2, '2025-09-15', 20, 10, 25, NOW()),
                    (2, '2025-09-16', 15, 8, 20, NOW())
            """);
        }
        
        private JobParameters createJobParameters() {
            return new JobParametersBuilder()
                .addString("startDate", START_DATE)
                .addString("endDate", END_DATE)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        }
    }
}
