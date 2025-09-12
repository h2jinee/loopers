package com.loopers.application.handler;

import com.loopers.application.service.CacheEvictionService;
import com.loopers.application.service.EventProcessingService;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionHandler {

    private static final String CONSUMER_NAME = "CACHE_EVICT";

    private final EventProcessingService eventProcessingService;
    private final CacheEvictionService cacheEvictionService;

    @Transactional
    public boolean handleEvent(KafkaEventMessage<?> message) {
        if (message == null) {
            log.warn("Null 메시지 수신");
            return false;
        }

        var eventId = message.getEventId();
        var eventType = message.getEventType();
        var aggregateId = message.getAggregateId();
        var eventVersion = message.getVersion() != null
            ? message.getVersion()
            : 0L;

        // 1. 캐시 무효화가 필요한 이벤트인지 체크
        if (!shouldEvictCache(message)) {
            log.debug("캐시 무효화 대상 아님 - type: {}", eventType);
            return false;
        }

        // 2. 처리 가능 체크
        if (!eventProcessingService.canProcessEvent(eventId, eventType, aggregateId, CONSUMER_NAME, eventVersion)) {
            return false;
        }

        // 3. 캐시 무효화 실행
        boolean cacheEvicted = evictCacheByEventType(message);

        // 4. 캐시 삭제한 경우에만 처리 기록
        if (cacheEvicted) {
            eventProcessingService.markAsProcessed(
                eventId, CONSUMER_NAME, eventType, aggregateId, eventVersion
            );
            log.info("캐시 무효화 완료 - eventId: {}, type: {}", eventId, eventType);
        }

        return cacheEvicted;
    }

    private boolean shouldEvictCache(KafkaEventMessage<?> message) {
        var payload = message.getPayload();
        return payload instanceof CatalogEventPayload.LikeAdded ||
            payload instanceof CatalogEventPayload.LikeRemoved ||
            payload instanceof CatalogEventPayload.StockChanged;
    }

    private boolean evictCacheByEventType(KafkaEventMessage<?> message) {
        var payload = message.getPayload();

        return switch (payload) {
            case CatalogEventPayload.LikeAdded likeAdded -> {
                cacheEvictionService.evictAllProductCaches(likeAdded.getProductId());
                yield true;
            }
            case CatalogEventPayload.LikeRemoved likeRemoved -> {
                cacheEvictionService.evictAllProductCaches(likeRemoved.getProductId());
                yield true;
            }
            case CatalogEventPayload.StockChanged stockChanged -> {
                // 재고가 0이 되었을 때만 캐시 삭제
                if (stockChanged.getCurrentQuantity() == 0) {
                    cacheEvictionService.evictAllProductCaches(stockChanged.getProductId());
                    log.info("재고 소진으로 캐시 삭제 - productId: {}", stockChanged.getProductId());
                    yield true;
                } else {
                    log.debug("재고 있음, 캐시 유지 - productId: {}, 재고: {}",
                        stockChanged.getProductId(), stockChanged.getCurrentQuantity());
                    yield false;
                }
            }
            case null -> {
                log.error("Null payload - eventType: {}", message.getEventType());
                yield false;
            }
            default -> {
                log.warn("예상치 못한 payload 타입 - type: {}", payload.getClass().getSimpleName());
                yield false;
            }
        };
    }
}
