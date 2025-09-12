package com.loopers.application.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.service.AuditLogService;
import com.loopers.application.service.EventProcessingService;
import com.loopers.application.service.dto.EventLogRequest;
import com.loopers.kafka.message.KafkaEventMessage;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogEventHandler {

    private static final String CONSUMER_NAME = "AUDIT_LOG";

    private final EventProcessingService eventProcessingService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;  // payload JSON으로 변환용

    @Transactional
    public boolean handleEvent(KafkaEventMessage<?> message, String topic,
        Integer partition, Long offset) {
        if (message == null) {
            log.warn("Null 메시지 수신 - topic: {}, partition: {}, offset: {}", topic, partition, offset);
            return false;
        }

        var eventId = message.getEventId();
        var eventType = message.getEventType();
        var aggregateId = message.getAggregateId();
        var eventVersion = message.getVersion() != null ? message.getVersion().longValue(): 0L;

        // 1. 처리 가능 체크
        if (!eventProcessingService.canProcessEvent(eventId, eventType, aggregateId, CONSUMER_NAME, eventVersion)) {
            return false;
        }

        // 2. 감사 로그 저장
        try {
            // Payload → JSON 문자열 (DB 저장용)
            String payloadJson = objectMapper.writeValueAsString(message.getPayload());

            // LocalDateTime → Long 변환
            Long timestamp = message.getTimestamp() != null
                ? message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();

            // DTO 사용
            EventLogRequest request = EventLogRequest.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateId(aggregateId)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .payloadJson(payloadJson)
                .timestamp(timestamp)
                .build();

            auditLogService.saveEventLog(request);
        } catch (JsonProcessingException e) {
            log.error("Payload JSON 변환 실패 - eventId: {}", eventId, e);
        }

        // 3. 처리 완료 기록
        eventProcessingService.markAsProcessed(
            eventId, CONSUMER_NAME, eventType, aggregateId, eventVersion
        );

        return true;
    }
}
