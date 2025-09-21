package com.loopers.batch.reader;

import com.loopers.batch.reader.dto.ProductMetricsAggregation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductMetricsReader {

    private final DataSource dataSource;

    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_TOTAL_LIKES = "total_likes";
    private static final String COLUMN_TOTAL_ORDERS = "total_orders";
    private static final String COLUMN_TOTAL_SALES = "total_sales";
    private static final int PAGE_SIZE = 1000;

    @Bean
    @StepScope
    public JdbcPagingItemReader<ProductMetricsAggregation> weeklyReader(
        @Value("#{jobParameters['startDate']}") String startDate,
        @Value("#{jobParameters['endDate']}") String endDate) {

        log.info("주간 Reader 생성 - 기간: {} ~ {}", startDate, endDate);
        return createReader("weeklyReader", startDate, endDate);
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<ProductMetricsAggregation> monthlyReader(
        @Value("#{jobParameters['startDate']}") String startDate,
        @Value("#{jobParameters['endDate']}") String endDate) {

        log.info("월간 Reader 생성 - 기간: {} ~ {}", startDate, endDate);
        return createReader("monthlyReader", startDate, endDate);
    }

    /**
     * 공통 Reader 생성 메서드
     */
    private JdbcPagingItemReader<ProductMetricsAggregation> createReader(
        String readerName, String startDate, String endDate) {

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("startDate", startDate);
        parameterValues.put("endDate", endDate);

        return new JdbcPagingItemReaderBuilder<ProductMetricsAggregation>()
            .name(readerName)
            .dataSource(dataSource)
            .pageSize(PAGE_SIZE)
            .fetchSize(PAGE_SIZE)
            .queryProvider(createQueryProvider())
            .parameterValues(parameterValues)
            .rowMapper((rs, rowNum) -> new ProductMetricsAggregation(
                rs.getLong(COLUMN_PRODUCT_ID),
                rs.getLong(COLUMN_TOTAL_LIKES),
                rs.getLong(COLUMN_TOTAL_ORDERS),
                rs.getLong(COLUMN_TOTAL_SALES)
            ))
            .build();
    }

    /**
     * Query Provider 생성
     */
    private MySqlPagingQueryProvider createQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();

        queryProvider.setSelectClause(
            "SELECT " + COLUMN_PRODUCT_ID + ", " +
                "SUM(like_count) as " + COLUMN_TOTAL_LIKES + ", " +
                "SUM(order_count) as " + COLUMN_TOTAL_ORDERS + ", " +
                "SUM(sales_quantity) as " + COLUMN_TOTAL_SALES
        );

        queryProvider.setFromClause("FROM product_metrics");
        queryProvider.setWhereClause("WHERE metric_date BETWEEN :startDate AND :endDate");
        queryProvider.setGroupClause("GROUP BY " + COLUMN_PRODUCT_ID);

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put(COLUMN_PRODUCT_ID, Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return queryProvider;
    }
}
