package com.loopers.batch.writer;

import com.loopers.domain.ranking.MonthlyRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyRankingWriter implements ItemWriter<MonthlyRanking> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    @SuppressWarnings("NullableProblems")
    public void write(Chunk<? extends MonthlyRanking> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO mv_product_rank_monthly 
                (product_id, period_start, period_end, score, like_count, order_count, sales_quantity, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                score = VALUES(score),
                like_count = VALUES(like_count),
                order_count = VALUES(order_count),
                sales_quantity = VALUES(sales_quantity),
                updated_at = NOW()
            """;

        List<Object[]> batchArgs = new ArrayList<>();
        for (MonthlyRanking item : items) {
            Object[] values = {
                item.getProductId(),
                item.getPeriodStart(),
                item.getPeriodEnd(),
                item.getScore(),
                item.getLikeCount(),
                item.getOrderCount(),
                item.getSalesQuantity()
            };
            batchArgs.add(values);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);

        log.info("월간 배치 저장 완료: {} 건", items.size());
    }
}
