package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EventLogJpaRepository extends JpaRepository<EventLog, Long> {
    Optional<EventLog> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}
