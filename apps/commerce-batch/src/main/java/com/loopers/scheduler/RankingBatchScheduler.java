package com.loopers.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    /**
     * 매일 오후 6시 - 최근 7일 집계
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void runWeeklyRanking() {
        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(6);

            JobParameters params = new JobParametersBuilder()
                .addString("startDate", startDate.toString())
                .addString("endDate", endDate.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            log.info("주간 랭킹 배치 시작 - 최근 7일: {} ~ {}", startDate, endDate);

            jobLauncher.run(weeklyRankingJob, params);

        } catch (Exception e) {
            log.error("주간 랭킹 배치 실행 실패", e);
        }
    }

    /**
     * 매일 오후 6시 30분 - 최근 30일 집계
     */
    @Scheduled(cron = "0 30 18 * * *")
    public void runMonthlyRanking() {
        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(29);

            JobParameters params = new JobParametersBuilder()
                .addString("startDate", startDate.toString())
                .addString("endDate", endDate.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            log.info("월간 랭킹 배치 시작 - 최근 30일: {} ~ {}", startDate, endDate);

            jobLauncher.run(monthlyRankingJob, params);

        } catch (Exception e) {
            log.error("월간 랭킹 배치 실행 실패", e);
        }
    }
}
