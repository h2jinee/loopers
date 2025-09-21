package com.loopers.batch.processor;

import com.loopers.batch.reader.dto.ProductMetricsAggregation;
import com.loopers.domain.ranking.WeeklyRanking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@StepScope
public class WeeklyRankingProcessor implements ItemProcessor<ProductMetricsAggregation, WeeklyRanking> {

    @Value("#{jobParameters['startDate']}")
    private String startDate;

    @Value("#{jobParameters['endDate']}")
    private String endDate;

    @Value("${batch.ranking.weights.like:1.0}")
    private double likeWeight;

    @Value("${batch.ranking.weights.order:100.0}")
    private double orderWeight;

    @Value("${batch.ranking.weights.sales:10.0}")
    private double salesWeight;

    @Override
    public WeeklyRanking process(ProductMetricsAggregation item) throws Exception {
        double score = calculateScore(item);

        WeeklyRanking ranking = WeeklyRanking.create(
            item.getProductId(),
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
            score,
            item.getTotalLikes(),
            item.getTotalOrders(),
            item.getTotalSales()
        );

        log.debug("주간 처리 완료 - productId: {}, score: {}", item.getProductId(), score);

        return ranking;
    }

    private double calculateScore(ProductMetricsAggregation item) {
        double likeScore = item.getTotalLikes() * likeWeight;
        double orderScore = item.getTotalOrders() * orderWeight;
        double salesScore = item.getTotalSales() * salesWeight;

        return likeScore + orderScore + salesScore;
    }
}
