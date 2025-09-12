package com.loopers.application.service;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessingService {

    private final EventHandledRepository eventHandledRepository;

    /**
     * 이벤트 처리 가능 여부 확인
     */
    public boolean canProcessEvent(String eventId, String eventType,
        String aggregateId, String consumerName,
        Long eventVersion) {
        // 1. 이미 처리된 이벤트 체크
        if (eventHandledRepository.existsByEventIdAndConsumerName(eventId, consumerName)) {
            log.debug("이미 처리된 이벤트 - eventId: {}, consumer: {}", eventId, consumerName);
            return false;
        }

        // 2. 버전 체크
        Optional<EventHandled> latestProcessed = eventHandledRepository
            .findLatestVersion(aggregateId, eventType, consumerName);

        if (latestProcessed.isPresent() &&
            latestProcessed.get().getEventVersion() >= eventVersion) {
            log.debug("구 버전 이벤트 - aggregateId: {}, type: {}, version: {} (latest: {})",
                aggregateId, eventType, eventVersion, latestProcessed.get().getEventVersion());
            return false;
        }

        return true;  // 처리 가능
    }

    /**
     * 이벤트 처리 완료 기록
     */
    public void markAsProcessed(String eventId, String consumerName, String eventType,
        String aggregateId, Long eventVersion) {
        EventHandled eventHandled = EventHandled.create(
            eventId,
            consumerName,
            eventType,
            aggregateId,
            eventVersion
        );
        eventHandledRepository.save(eventHandled);
        log.debug("이벤트 처리 기록 - eventId: {}, consumer: {}", eventId, consumerName);
    }
}
