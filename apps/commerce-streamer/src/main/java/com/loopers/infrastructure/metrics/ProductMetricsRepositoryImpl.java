package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository jpaRepository;

    @Override
    public ProductMetrics save(ProductMetrics metrics) {
        return jpaRepository.save(metrics);
    }

    @Override
    public Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate date) {
        return jpaRepository.findByProductIdAndMetricDate(productId, date);
    }
}
