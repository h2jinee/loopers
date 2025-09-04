package com.loopers.domain.event;

public interface EventHandledRepository {
    EventHandled save(EventHandled eventHandled);
    boolean existsByEventIdAndConsumerName(String eventId, String consumerName);
}
