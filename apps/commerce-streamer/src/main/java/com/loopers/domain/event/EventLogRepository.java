package com.loopers.domain.event;

import java.util.Optional;

public interface EventLogRepository {
    EventLog save(EventLog eventLog);
    Optional<EventLog> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}
