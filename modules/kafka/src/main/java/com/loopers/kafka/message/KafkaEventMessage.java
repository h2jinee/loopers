package com.loopers.kafka.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class KafkaEventMessage<T> {

    private String eventId;
    private String eventType;
    private String aggregateId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Long version;
    private T payload;

    public static <T> KafkaEventMessage<T> of(String eventType, String aggregateId, T payload) {
        return KafkaEventMessage.<T>builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .version(1L)
            .eventType(eventType)
            .aggregateId(aggregateId)
            .payload(payload)
            .build();
    }
}
