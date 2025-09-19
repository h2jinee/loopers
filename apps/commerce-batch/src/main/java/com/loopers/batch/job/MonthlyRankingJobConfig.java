package com.loopers.batch.job;

import com.loopers.batch.processor.MonthlyRankingProcessor;
import com.loopers.batch.reader.dto.ProductMetricsAggregation;
import com.loopers.batch.writer.MonthlyRankingWriter;
import com.loopers.domain.ranking.MonthlyRanking;
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
public class MonthlyRankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MonthlyRankingProcessor processor;
    private final MonthlyRankingWriter writer;

    @Bean
    public Job monthlyRankingJob(Step monthlyRankingStep) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
            .start(monthlyRankingStep)
            .build();
    }

    @Bean
    public Step monthlyRankingStep(
        JdbcPagingItemReader<ProductMetricsAggregation> monthlyReader) {

        return new StepBuilder("monthlyRankingStep", jobRepository)
            .<ProductMetricsAggregation, MonthlyRanking>chunk(1000, transactionManager)
            .reader(monthlyReader)
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
