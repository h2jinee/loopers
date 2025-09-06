package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Kafka Consumer에서 처리 실패한 메시지 저장
 * (Producer 실패는 commerce-api의 DeadLetterEvent에 저장)
 */
@Entity
@Table(name = "dead_letter_kafka_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DeadLetterKafkaEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "original_topic", nullable = false)
	private String originalTopic;

	@Lob
	@Column(name = "original_message", nullable = false, columnDefinition = "TEXT")
	private String originalMessage;

	@Column(name = "consumer_name", nullable = false)
	private String consumerName;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "retry_count")
	@Builder.Default
	private Integer retryCount = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	@Builder.Default
	private DlqStatus status = DlqStatus.PENDING;

	@CreatedDate
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	public enum DlqStatus {
		PENDING,    // 처리 대기
		RETRYING,   // 재시도 중
		RESOLVED,   // 해결됨
		FAILED       // 최종 실패 (수동 개입 필요)
	}

	public static DeadLetterKafkaEvent create(
		String originalTopic,
		String originalMessage,
		String consumerName,
		String errorMessage,
		Integer retryCount
	) {
		return DeadLetterKafkaEvent.builder()
			.originalTopic(originalTopic)
			.originalMessage(originalMessage)
			.consumerName(consumerName)
			.errorMessage(errorMessage)
			.retryCount(retryCount)
			.build();
	}

	public void incrementRetry() {
		this.retryCount++;
		this.status = DlqStatus.RETRYING;
	}

	public void markAsResolved() {
		this.status = DlqStatus.RESOLVED;
		this.resolvedAt = LocalDateTime.now();
	}

	public void markAsFailed() {
		this.status = DlqStatus.FAILED;
	}
}
