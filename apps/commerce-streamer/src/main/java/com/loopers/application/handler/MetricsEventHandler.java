package com.loopers.application.handler;

import com.loopers.application.service.EventProcessingService;
import com.loopers.application.service.MetricsService;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import com.loopers.kafka.message.payload.OrderEventPayload;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsEventHandler {

    private static final String CONSUMER_NAME = "METRICS";

    private final EventProcessingService eventProcessingService;
    private final MetricsService metricsService;

    @Transactional
    public boolean handleEvent(KafkaEventMessage<?> message) {
        if (message == null) {
            log.error("Null 메시지 수신");
            return false;
        }

        var eventId = message.getEventId();
        var eventType = message.getEventType();
        var aggregateId = message.getAggregateId();
        var eventVersion = Optional.ofNullable(message.getVersion())
            .orElse(0L);  // 버전 없으면 0 (가장 오래된 것으로 처리)

        // 처리 가능 체크
        if (!eventProcessingService.canProcessEvent(eventId, eventType, aggregateId, CONSUMER_NAME, eventVersion)) {
            return false;
        }

        // 메트릭 업데이트
        try {
            updateMetrics(message);
        } catch (Exception e) {
            log.error("메트릭 업데이트 실패 - eventId: {}", eventId, e);
            throw e;  // 트랜잭션 롤백
        }

        // 처리 완료 기록
        eventProcessingService.markAsProcessed(
            eventId, CONSUMER_NAME, eventType, aggregateId, eventVersion
        );

        return true;
    }

    private void updateMetrics(KafkaEventMessage<?> message) {
        var payload = message.getPayload();

        switch (payload) {
            case CatalogEventPayload.LikeChanged likeChanged ->
                metricsService.setLikeCount(
                    likeChanged.getProductId(),
                    likeChanged.getTotalLikeCount()
                );

            case OrderEventPayload.OrderCreated orderCreated when
                orderCreated.getOrderItems() != null && !orderCreated.getOrderItems().isEmpty() -> {
                orderCreated.getOrderItems().forEach(item ->
                    metricsService.aggregateOrderMetrics(
                        item.getProductId(),
                        (long) item.getQuantity()
                    )
                );
            }

            case OrderEventPayload.OrderCreated orderCreated ->
                log.warn("주문 항목이 없는 주문 - orderId: {}", orderCreated.getOrderId());

            case null ->
                log.error("Null payload - eventType: {}", message.getEventType());

            default ->
                log.debug("메트릭 처리 대상 아님 - type: {}", message.getEventType());
        }
    }
}
