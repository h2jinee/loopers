package com.loopers.application.service;

import com.loopers.application.service.dto.EventLogRequest;
import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final EventLogRepository eventLogRepository;

    @Transactional
    public void saveEventLog(EventLogRequest request) {

        LocalDateTime eventTimestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(request.getTimestamp()),
            ZoneId.systemDefault()
        );

        EventLog eventLog = EventLog.create(
            request.getEventId(),
            request.getEventType(),
            request.getAggregateId(),
            request.getTopic(),
            request.getPartition(),
            request.getOffset(),
            request.getPayloadJson(),
            eventTimestamp
        );

        eventLogRepository.save(eventLog);

        log.info("감사 로그 저장 - eventId: {}, type: {}, topic: {}", request.getEventId(), request.getEventType(), request.getTopic());
    }
}
