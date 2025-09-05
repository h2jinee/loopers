package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.interfaces.consumer.support.DlqPublisher;
import com.loopers.kafka.EventTypes;
import com.loopers.kafka.KafkaTopics;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import com.loopers.kafka.message.payload.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 메트릭 집계 Consumer
 * 일별 상품 통계를 product_metrics 테이블에 UPSERT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private static final String CONSUMER_NAME = "METRICS";

    private final ProductMetricsRepository productMetricsRepository;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;
	private final DlqPublisher dlqPublisher;

	@KafkaListener(
		topics = {KafkaTopics.CATALOG_EVENTS, KafkaTopics.ORDER_EVENTS},
		groupId = "metrics-batch-group",
		containerFactory = "BATCH_LISTENER_DEFAULT"
	)
	@Transactional
	public void consumeBatch(
		List<String> messages,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		Acknowledgment ack
	) {
		log.info("배치 처리 시작 - {} 건", messages.size());

		int processedCount = 0;
		int failedCount = 0;

		for (String messageJson : messages) {
			try {
				// JSON 파싱
				KafkaEventMessage<?> message = objectMapper.readValue(
					messageJson,
					objectMapper.getTypeFactory().constructParametricType(
						KafkaEventMessage.class,
						Object.class
					)
				);

				String eventId = message.getEventId();

				// 1. 멱등성 체크
				if (eventHandledRepository.existsByEventIdAndConsumerName(eventId, CONSUMER_NAME)) {
					log.debug("이미 처리된 이벤트 스킵 - eventId: {}", eventId);
					continue;
				}

				// 2. Version 체크
				Long eventVersion = message.getVersion() != null
					? message.getVersion().longValue()
					: System.currentTimeMillis() / 1000;

				Optional<EventHandled> latestProcessed = eventHandledRepository
					.findLatestVersion(message.getAggregateId(), CONSUMER_NAME);

				if (latestProcessed.isPresent() &&
					latestProcessed.get().getEventVersion() >= eventVersion) {
					log.debug("구 버전 이벤트 스킵 - eventId: {}", eventId);
					continue;
				}

				// 3. 이벤트 처리
				switch (message.getEventType()) {
					case EventTypes.LIKE_ADDED -> handleLikeAdded(message);
					case EventTypes.LIKE_REMOVED -> handleLikeRemoved(message);
					case EventTypes.ORDER_CREATED -> handleOrderCreated(message);
					default -> log.debug("메트릭 처리 대상 아님 - type: {}", message.getEventType());
				}

				// 4. 처리 완료 기록
				eventHandledRepository.save(
					EventHandled.create(
						eventId,
						CONSUMER_NAME,
						message.getEventType(),
						message.getAggregateId(),
						eventVersion
					)
				);

				processedCount++;

			} catch (Exception e) {
				log.error("개별 메시지 처리 실패", e);
				failedCount++;

				// DLQ로 전송
				dlqPublisher.sendToDlq(
					topic,
					messageJson,
					CONSUMER_NAME,
					e.getMessage()
				);
			}
		}

		// 5. 배치 전체 ACK
		ack.acknowledge();
		log.info("배치 처리 완료 - 처리: {}/{} 건", processedCount, messages.size());
	}

    /**
     * 좋아요 추가 처리
     */
    private void handleLikeAdded(KafkaEventMessage<?> message) {
        CatalogEventPayload.LikeAdded payload =
            objectMapper.convertValue(message.getPayload(), CatalogEventPayload.LikeAdded.class);

        Long productId = payload.getProductId();
        LocalDate today = LocalDate.now();

        // 오늘 날짜의 메트릭 조회 or 생성
        ProductMetrics metrics = productMetricsRepository
            .findByProductIdAndMetricDate(productId, today)
            .orElse(ProductMetrics.builder()
                .productId(productId)
                .metricDate(today)
                .likeCount(0L)
                .orderCount(0L)
                .salesQuantity(0L)
                .build());

        // 좋아요 수 증가
        metrics.addLike();

        productMetricsRepository.save(metrics);
        log.info("좋아요 메트릭 업데이트 - productId: {}, likeCount: {}",
            productId, metrics.getLikeCount());
    }

    /**
     * 좋아요 제거 처리
     */
    private void handleLikeRemoved(KafkaEventMessage<?> message) {
        CatalogEventPayload.LikeRemoved payload =
            objectMapper.convertValue(message.getPayload(), CatalogEventPayload.LikeRemoved.class);

        Long productId = payload.getProductId();
        LocalDate today = LocalDate.now();

        ProductMetrics metrics = productMetricsRepository
            .findByProductIdAndMetricDate(productId, today)
            .orElse(ProductMetrics.builder()
                .productId(productId)
                .metricDate(today)
                .likeCount(0L)
                .orderCount(0L)
                .salesQuantity(0L)
                .build());

        metrics.removeLike();

        productMetricsRepository.save(metrics);
        log.info("좋아요 메트릭 감소 - productId: {}, likeCount: {}",
            productId, metrics.getLikeCount());
    }

    /**
     * 주문 생성 처리
     */
    private void handleOrderCreated(KafkaEventMessage<?> message) {
        OrderEventPayload.OrderCreated payload =
            objectMapper.convertValue(message.getPayload(), OrderEventPayload.OrderCreated.class);

        LocalDate today = LocalDate.now();

        // 주문의 각 상품별로 처리
        for (OrderEventPayload.OrderItem item : payload.getItems()) {
            Long productId = item.getProductId();

            ProductMetrics metrics = productMetricsRepository
                .findByProductIdAndMetricDate(productId, today)
                .orElse(ProductMetrics.builder()
                    .productId(productId)
                    .metricDate(today)
                    .likeCount(0L)
                    .orderCount(0L)
                    .salesQuantity(0L)
                    .build());

            // 주문 수와 판매량 증가
            metrics.addOrder(Long.valueOf(item.getQuantity()));

            productMetricsRepository.save(metrics);
            log.info("주문 메트릭 업데이트 - productId: {}, orderCount: {}, salesQty: {}",
                productId, metrics.getOrderCount(), metrics.getSalesQuantity());
        }
    }
}
