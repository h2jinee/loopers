package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 감사 로그 엔티티
 * 모든 Kafka 이벤트를 저장하는 테이블
 */
@Entity
@Table(name = "event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;  // 이벤트 고유 ID (UUID)

    @Column(name = "event_type", nullable = false)
    private String eventType;  // OrderCreated, LikeAdded 등

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;  // 도메인 ID (ex. productId, orderId etc)

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "partition_number")
    private Integer partitionNumber;

    @Column(name = "offset_value")
    private Long offsetValue;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static EventLog create(
        String eventId,
        String eventType,
        String aggregateId,
        String topicName,
        Integer partitionNumber,
        Long offsetValue,
        String payload,
        LocalDateTime eventTimestamp
    ) {
        return EventLog.builder()
            .eventId(eventId)
            .eventType(eventType)
            .aggregateId(aggregateId)
            .topicName(topicName)
            .partitionNumber(partitionNumber)
            .offsetValue(offsetValue)
            .payload(payload)
            .eventTimestamp(eventTimestamp)
            .build();
    }
}
