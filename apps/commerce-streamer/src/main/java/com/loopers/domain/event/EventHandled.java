package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 처리 완료 엔티티
 * 멱등성 보장: 같은 이벤트가 여러번 와도 한번만 처리
 */
@Entity
@Table(
    name = "event_handled",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_event_handled",
            columnNames = {"event_id", "consumer_name"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EventHandled {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;  // AUDIT_LOG, METRICS, CACHE_EVICT

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static EventHandled create(
        String eventId,
        String consumerName,
        String eventType
    ) {
        return EventHandled.builder()
            .eventId(eventId)
            .consumerName(consumerName)
            .eventType(eventType)
            .processedAt(LocalDateTime.now())
            .build();
    }
}
