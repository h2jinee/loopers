package com.loopers.infrastructure.event.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.event.Event;
import com.loopers.application.event.EventPublisher;
import com.loopers.domain.event.Outbox;
import com.loopers.domain.event.OutboxRepository;
import com.loopers.kafka.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher implements EventPublisher {

	private final OutboxRepository outboxRepository;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void publish(Event event) {
		try {
			// 1. 토픽 결정
			String topic = resolveTopicFor(event.getEventType());

			// 2. 이벤트를 JSON으로 변환
			String payload = objectMapper.writeValueAsString(event);

			// 3. Outbox 테이블에 저장
			Outbox outbox = Outbox.create(
				event.getAggregateId(),
				event.getEventType(),
				topic,
				payload,
				event.getVersion()
			);

			outboxRepository.save(outbox);

			log.debug("이벤트를 Outbox에 저장 - type: {}, aggregateId: {}",
				event.getEventType(), event.getAggregateId());

		} catch (Exception e) {
			log.error("Outbox 저장 실패 - eventType: {}", event.getEventType(), e);
			throw new RuntimeException("이벤트 저장 실패", e);
		}
	}

    private String resolveTopicFor(String eventType) {
        // 이벤트 타입에 따라 토픽 결정
        if (eventType.contains("Like") || eventType.contains("Added") ||
            eventType.contains("Removed") || eventType.contains("Stock")) {
            return KafkaTopics.CATALOG_EVENTS;
        }
        if (eventType.contains("Order") || eventType.contains("Payment")) {
            return KafkaTopics.ORDER_EVENTS;
        }

        log.warn("알 수 없는 이벤트 타입: {}, 기본 토픽 사용", eventType);
        return KafkaTopics.CATALOG_EVENTS;
    }
}
