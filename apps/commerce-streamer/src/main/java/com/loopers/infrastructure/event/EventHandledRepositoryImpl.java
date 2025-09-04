package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventHandledRepositoryImpl implements EventHandledRepository {

    private final EventHandledJpaRepository jpaRepository;

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return jpaRepository.save(eventHandled);
    }

    @Override
    public boolean existsByEventIdAndConsumerName(String eventId, String consumerName) {
        return jpaRepository.existsByEventIdAndConsumerName(eventId, consumerName);
    }
}
