package com.loopers.domain.metrics;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricsRepository {
    ProductMetrics save(ProductMetrics metrics);
    Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate date);
}
