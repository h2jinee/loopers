package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

    private final EventLogJpaRepository jpaRepository;

    @Override
    public EventLog save(EventLog eventLog) {
        return jpaRepository.save(eventLog);
    }

    @Override
    public Optional<EventLog> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }
}
