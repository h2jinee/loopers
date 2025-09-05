package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.loopers.kafka.EventTypes;
import com.loopers.kafka.KafkaTopics;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionConsumer {

    private static final String CONSUMER_NAME = "CACHE_EVICT";
    private static final String PRODUCT_DETAIL_KEY = "product:detail:";
    private static final String PRODUCT_LIST_KEY = "product:list:*";
    private static final String PRODUCT_POPULAR_KEY = "product:popular:*";
    private static final String PRODUCT_RECOMMEND_KEY = "product:recommend:*";

    private final EventHandledRepository eventHandledRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

	@KafkaListener(
		topics = {KafkaTopics.CATALOG_EVENTS},
		groupId = "cache-eviction-group",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(
		String messageJson,
		Acknowledgment ack
	) throws JsonProcessingException {
		// JSON 파싱
		KafkaEventMessage<?> message = objectMapper.readValue(
			messageJson,
			objectMapper.getTypeFactory().constructParametricType(
				KafkaEventMessage.class,
				Object.class
			)
		);

		String eventId = message.getEventId();

		try {
			// 1. 멱등성 체크
			if (eventHandledRepository.existsByEventIdAndConsumerName(eventId, CONSUMER_NAME)) {
				log.debug("이미 처리된 이벤트 스킵 - eventId: {}", eventId);
				ack.acknowledge();
				return;
			}

			// 2. Version 체크 (순서가 뒤바뀐 이벤트 처리)
			Long eventVersion = message.getVersion() != null
				? message.getVersion().longValue()
				: System.currentTimeMillis() / 1000;

			Optional<EventHandled> latestProcessed = eventHandledRepository
				.findLatestVersion(message.getAggregateId(), CONSUMER_NAME);

			if (latestProcessed.isPresent() &&
				latestProcessed.get().getEventVersion() >= eventVersion) {
				log.warn("구 버전 이벤트 스킵 (Cache) - eventId: {}, version: {}, latestVersion: {}",
					eventId, eventVersion, latestProcessed.get().getEventVersion());
				ack.acknowledge();
				return;
			}

			// 3. 캐시 무효화가 필요한 이벤트만 처리
			boolean cacheEvicted = false;

			switch (message.getEventType()) {
				case EventTypes.LIKE_ADDED,
					 EventTypes.LIKE_REMOVED -> {
					evictProductCache(message);
					cacheEvicted = true;
				}
				case EventTypes.STOCK_CHANGED -> {
					// 재고 변경
					CatalogEventPayload.StockChanged stockEvent =
						objectMapper.convertValue(message.getPayload(), CatalogEventPayload.StockChanged.class);

					// 재고가 0이 되었을 때만 캐시 삭제
					if (stockEvent.getCurrentQuantity() == 0) {
						evictProductCache(message);
						cacheEvicted = true;
						log.info("재고 소진 - productId: {} 캐시 삭제", stockEvent.getProductId());
					} else {
						log.debug("재고 변경되었지만 소진 아님 - productId: {}, 현재재고: {}",
							stockEvent.getProductId(), stockEvent.getCurrentQuantity());
					}
				}
				default -> log.debug("캐시 무효화 대상 아님 - type: {}", message.getEventType());
			}

			// 4. 캐시 삭제한 경우에만 처리 기록
			if (cacheEvicted) {
				eventHandledRepository.save(
					EventHandled.create(
						eventId,
						CONSUMER_NAME,
						message.getEventType(),
						message.getAggregateId(),
						eventVersion
					)
				);
				log.info("캐시 무효화 완료 - eventId: {}, type: {}",
					eventId, message.getEventType());
			}

			// 5. ACK
			ack.acknowledge();

		} catch (Exception e) {
			log.error("캐시 무효화 실패 - eventId: {}", eventId, e);
		}
	}

    /**
     * 상품 관련 캐시 삭제
     */
    private void evictProductCache(KafkaEventMessage<?> message) {
        Long productId = extractProductId(message);
        if (productId == null) {
            log.warn("ProductId 추출 실패 - eventType: {}", message.getEventType());
            return;
        }

        // 1. 상품 상세 캐시 삭제
        String detailKey = PRODUCT_DETAIL_KEY + productId;
        Boolean deleted = redisTemplate.delete(detailKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("상품 상세 캐시 삭제 - key: {}", detailKey);
        }

        // 2. 나머지 캐시 삭제
        deleteKeysByPattern(PRODUCT_LIST_KEY, "상품 목록");
        deleteKeysByPattern(PRODUCT_POPULAR_KEY, "인기 상품");
        deleteKeysByPattern(PRODUCT_RECOMMEND_KEY, "추천 상품");
    }

    /**
     * 패턴에 매칭되는 키들 삭제
     */
    private void deleteKeysByPattern(String pattern, String description) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("{} 캐시 삭제 - count: {}", description, deletedCount);
        }
    }

    /**
     * 이벤트에서 ProductId 추출
     */
    private Long extractProductId(KafkaEventMessage<?> message) {
        Object payload = message.getPayload();

        if (payload instanceof CatalogEventPayload.LikeAdded likeAdded) {
            return likeAdded.getProductId();
        } else if (payload instanceof CatalogEventPayload.LikeRemoved likeRemoved) {
            return likeRemoved.getProductId();
        } else if (payload instanceof CatalogEventPayload.StockChanged stockChanged) {
            return stockChanged.getProductId();
        }

        // aggregateId에서 추출 시도
        try {
            return Long.parseLong(message.getAggregateId());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
