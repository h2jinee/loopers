package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Outbox extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "topic", nullable = false)
	private String topic;

	@Lob
	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@Builder.Default
	private OutboxStatus status = OutboxStatus.PENDING;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "retry_count")
	@Builder.Default
	private Integer retryCount = 0;

	@Column(name = "event_version")
	private Long eventVersion;

	@Version
	private Long version;

	public enum OutboxStatus {
		PENDING,    // 발행 대기
		PUBLISHED,  // 발행 완료
		FAILED      // 발행 실패
	}

	public static Outbox create(String aggregateId, String eventType,
		String topic, String payload, Long eventVersion) {
		return Outbox.builder()
			.aggregateId(aggregateId)
			.eventType(eventType)
			.topic(topic)
			.payload(payload)
			.eventVersion(eventVersion)
			.build();
	}

	// 발행 완료 처리
	public void markAsPublished() {
		this.status = OutboxStatus.PUBLISHED;
		this.processedAt = LocalDateTime.now();
	}

	// 발행 실패 처리
	public void markAsFailed() {
		this.status = OutboxStatus.FAILED;
		this.retryCount++;
	}

	// 재시도 가능 여부
	public boolean canRetry() {
		return this.retryCount < 3;  // 최대 3회 재시도
	}
}
