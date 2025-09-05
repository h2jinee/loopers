package com.loopers.interfaces.consumer;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import com.loopers.kafka.KafkaTopics;
import com.loopers.kafka.message.KafkaEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogConsumer {

    private static final String CONSUMER_NAME = "AUDIT_LOG";

    private final EventLogRepository eventLogRepository;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENTS, KafkaTopics.ORDER_EVENTS},
        groupId = "audit-log-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        String messageJson,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
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
            log.debug("이벤트 수신 - eventId: {}, type: {}, topic: {}",
                eventId, message.getEventType(), topic);

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
				log.warn("구 버전 이벤트 스킵 - eventId: {}, version: {}, latestVersion: {}",
					eventId, eventVersion, latestProcessed.get().getEventVersion());
				ack.acknowledge();
				return;
			}

            // 3. EventLog 저장
            EventLog eventLog = EventLog.create(
                eventId,
                message.getEventType(),
                message.getAggregateId(),
                topic,
                partition,
                offset,
                objectMapper.writeValueAsString(message.getPayload()),
                message.getTimestamp()
            );

            eventLogRepository.save(eventLog);
            log.info("이벤트 로그 저장 완료 - eventId: {}, type: {}",
                eventId, message.getEventType());

			// 4. 처리 완료 기록 (aggregateId와 version 포함)
			EventHandled eventHandled = EventHandled.create(
				eventId,
				CONSUMER_NAME,
				message.getEventType(),
				message.getAggregateId(),
				eventVersion  // 추가
			);

            eventHandledRepository.save(eventHandled);

            // 5. ACK
            ack.acknowledge();
            log.debug("이벤트 처리 완료 및 ACK - eventId: {}", eventId);

        } catch (Exception e) {
            log.error("이벤트 처리 실패 - eventId: {}, error: {}",
                eventId, e.getMessage(), e);
        }
    }
}
