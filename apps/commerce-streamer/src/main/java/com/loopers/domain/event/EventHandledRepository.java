package com.loopers.domain.event;

import java.util.Optional;

public interface EventHandledRepository {
    EventHandled save(EventHandled eventHandled);

    boolean existsByEventIdAndConsumerName(String eventId, String consumerName);

	Optional<EventHandled> findLatestVersion(String aggregateId, String eventType, String consumerName);
}
