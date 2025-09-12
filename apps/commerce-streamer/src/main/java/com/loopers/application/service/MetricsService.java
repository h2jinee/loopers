package com.loopers.application.service;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    /**
     * 좋아요 추가 메트릭 집계
     */
    @Transactional
    public void incrementLikeCount(Long productId) {
        ProductMetrics metrics = findOrCreateMetrics(productId);

        metrics.addLike();
        productMetricsRepository.save(metrics);

        log.info("좋아요 메트릭 증가 - productId: {}, likeCount: {}",
            productId, metrics.getLikeCount());
    }

    /**
     * 좋아요 제거 메트릭 집계
     */
    @Transactional
    public void decrementLikeCount(Long productId) {
        ProductMetrics metrics = findOrCreateMetrics(productId);

        metrics.removeLike();
        productMetricsRepository.save(metrics);

        log.info("좋아요 메트릭 감소 - productId: {}, likeCount: {}",
            productId, metrics.getLikeCount());
    }

    /**
     * 주문 메트릭 집계
     */
    @Transactional
    public void aggregateOrderMetrics(Long productId, Long quantity) {
        ProductMetrics metrics = findOrCreateMetrics(productId);

        metrics.addOrder(quantity);
        productMetricsRepository.save(metrics);

        log.info("주문 메트릭 업데이트 - productId: {}, orderCount: {}, salesQty: {}",
            productId, metrics.getOrderCount(), metrics.getSalesQuantity());
    }

    private ProductMetrics findOrCreateMetrics(Long productId) {
        return productMetricsRepository
            .findByProductIdAndMetricDate(productId, LocalDate.now())
            .orElse(ProductMetrics.builder()
                .productId(productId)
                .metricDate(LocalDate.now())
                .likeCount(0L)
                .orderCount(0L)
                .salesQuantity(0L)
                .build());
    }
}
