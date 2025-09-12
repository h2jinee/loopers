package com.loopers.application.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventLogRequest {
    private final String eventId;
    private final String eventType;
    private final String aggregateId;
    private final String topic;
    private final Integer partition;
    private final Long offset;
    private final String payloadJson;
    private final Long timestamp;
}
