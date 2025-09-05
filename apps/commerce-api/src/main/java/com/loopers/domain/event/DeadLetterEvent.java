package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 최종 실패한 이벤트 저장 테이블
 */
@Entity
@Table(name = "dead_letter_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DeadLetterEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "original_outbox_id", nullable = false)
	private Long originalOutboxId;  // 원본 Outbox ID

	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "topic", nullable = false)
	private String topic;

	@Lob
	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "retry_count")
	private Integer retryCount;

	@Column(name = "last_retry_at")
	private LocalDateTime lastRetryAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	@Builder.Default
	private DeadLetterStatus status = DeadLetterStatus.FAILED;

	public enum DeadLetterStatus {
		FAILED,         // 실패 상태
		REPROCESSING,   // 재처리 중
		RESOLVED        // 해결됨
	}

	public static DeadLetterEvent fromOutbox(Outbox outbox, String failureReason) {
		return DeadLetterEvent.builder()
			.originalOutboxId(outbox.getId())
			.aggregateId(outbox.getAggregateId())
			.eventType(outbox.getEventType())
			.topic(outbox.getTopic())
			.payload(outbox.getPayload())
			.failureReason(failureReason)
			.retryCount(outbox.getRetryCount())
			.lastRetryAt(LocalDateTime.now())
			.build();
	}

	// 재처리 시작
	public void startReprocessing() {
		this.status = DeadLetterStatus.REPROCESSING;
	}

	// 해결 처리
	public void markAsResolved() {
		this.status = DeadLetterStatus.RESOLVED;
	}
}
