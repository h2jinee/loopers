package com.loopers.batch.reader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Reader가 product_metrics 테이블에서 읽어온 집계 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMetricsAggregation {

    private Long productId;
    private Long totalLikes;
    private Long totalOrders;
    private Long totalSales;
}
