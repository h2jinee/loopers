package com.loopers.batch.job;

import com.loopers.batch.processor.WeeklyRankingProcessor;
import com.loopers.batch.reader.dto.ProductMetricsAggregation;
import com.loopers.batch.writer.WeeklyRankingWriter;
import com.loopers.domain.ranking.WeeklyRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeeklyRankingJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeeklyRankingProcessor processor;
    private final WeeklyRankingWriter writer;
    
    @Bean
    public Job weeklyRankingJob(Step weeklyRankingStep) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
            .start(weeklyRankingStep)
            .build();
    }
    
    @Bean
    public Step weeklyRankingStep(
            JdbcPagingItemReader<ProductMetricsAggregation> weeklyReader) {
        
        return new StepBuilder("weeklyRankingStep", jobRepository)
            .<ProductMetricsAggregation, WeeklyRanking>chunk(1000, transactionManager)
            .reader(weeklyReader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .retryLimit(3)
            .retry(Exception.class)
            .skipLimit(100)
            .skip(Exception.class)
            .noSkip(IllegalArgumentException.class)
            .build();
    }
}
