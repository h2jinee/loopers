package com.loopers.application.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockApplicationEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publish(StockChanged event) {
        eventPublisher.publishEvent(event);
    }
}
