package com.loopers.infrastructure.event.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.DeadLetterEvent;
import com.loopers.domain.event.DeadLetterEventRepository;
import com.loopers.domain.event.Outbox;
import com.loopers.domain.event.OutboxRepository;
import com.loopers.infrastructure.event.kafka.KafkaEventPublisher;
import com.loopers.kafka.message.KafkaEventMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

	private final OutboxRepository outboxRepository;
	private final KafkaEventPublisher kafkaEventPublisher;
	private final ObjectMapper objectMapper;
	private final DeadLetterEventRepository deadLetterEventRepository;

	/**
	 * 5초마다 실행되는 스케줄러
	 * PENDING 상태인 이벤트 Kafka로 발행
	 */
	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void processOutboxEvents() {
		try {
			// 1. PENDING 상태인 이벤트들 조회 (최대 100개씩 처리)
			List<Outbox> pendingEvents = outboxRepository
				.findByStatusOrderByCreatedAt(
					Outbox.OutboxStatus.PENDING,
					PageRequest.of(0, 100)
				);

			if (pendingEvents.isEmpty()) {
				return;
			}

			log.info("Outbox 이벤트 처리 시작 - {} 건", pendingEvents.size());

			for (Outbox outbox : pendingEvents) {
				processEvent(outbox);
			}

		} catch (Exception e) {
			log.error("Outbox 이벤트 처리 중 오류", e);
		}
	}

	/**
	 * 실패한 이벤트 재처리 (1분마다 실행)
	 */
	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void retryFailedEvents() {
		try {
			// 재시도 횟수가 3회 미만인 실패 이벤트 조회
			List<Outbox> failedEvents = outboxRepository
				.findFailedEventsForRetry(3, PageRequest.of(0, 50));

			if (failedEvents.isEmpty()) {
				return;
			}

			log.info("실패 이벤트 재처리 시작 - {} 건", failedEvents.size());

			for (Outbox outbox : failedEvents) {
				if (outbox.canRetry()) {
					processEvent(outbox);
				}
			}

		} catch (Exception e) {
			log.error("실패 이벤트 재처리 중 오류", e);
		}
	}

	/**
	 * 개별 이벤트 처리
	 */
	private void processEvent(Outbox outbox) {
		try {
			// 1. payload를 Object로 파싱
			Object payload = objectMapper.readValue(outbox.getPayload(), Object.class);

			// 2. KafkaEventMessage 생성
			KafkaEventMessage<Object> message = KafkaEventMessage.<Object>builder()
				.eventId(UUID.randomUUID().toString())
				.eventType(outbox.getEventType())
				.aggregateId(outbox.getAggregateId())
				.timestamp(LocalDateTime.now())
                .version(outbox.getEventVersion() != null
                    ? outbox.getEventVersion()
                    : 1L)
				.payload(payload)
				.build();

			// 3. Kafka로 발행
			kafkaEventPublisher.publish(
				outbox.getTopic(),
				outbox.getAggregateId(),  // Partition Key로 사용
				message
			);

			// 4. 발행 성공 → 상태 업데이트
			outbox.markAsPublished();
			outboxRepository.save(outbox);

			log.info(
				"Outbox 이벤트 발행 성공 - id: {}, type: {}",
				outbox.getId(), outbox.getEventType());

		} catch (Exception e) {
			// 5. 발행 실패 → 실패 처리
			log.error(
				"Outbox 이벤트 발행 실패 - id: {}, type: {}",
				outbox.getId(), outbox.getEventType(), e);

			outbox.markAsFailed();

			// 재시도 가능한지 확인
			if (!outbox.canRetry()) {
				// 최종 실패 → DeadLetter로 이동
				moveToDeadLetter(outbox, e.getMessage());
			} else {
				// 재시도 대기
				outboxRepository.save(outbox);
			}
		}
	}

	/**
	 * 최종 실패한 이벤트를 DeadLetter로 이동
	 */
	private void moveToDeadLetter(Outbox outbox, String failureReason) {
		try {
			// 이미 DeadLetter에 있는지 확인 (중복 방지)
			if (deadLetterEventRepository.existsByOriginalOutboxId(outbox.getId())) {
				log.warn("이미 DeadLetter에 존재 - outboxId: {}", outbox.getId());
				return;
			}

			// DeadLetter로 이동
			DeadLetterEvent deadLetter = DeadLetterEvent.fromOutbox(
				outbox,
				failureReason != null ? failureReason : "최대 재시도 횟수 초과"
			);

			deadLetterEventRepository.save(deadLetter);

			// Outbox에서 상태를 FAILED로 유지 (기록 보존)
			outbox.markAsFailed();
			outboxRepository.save(outbox);

			log.error(
				"이벤트 DeadLetter로 이동 - outboxId: {}, eventType: {}, retryCount: {}",
				outbox.getId(), outbox.getEventType(), outbox.getRetryCount());

		} catch (Exception ex) {
			log.error("DeadLetter 이동 실패 - outboxId: {}", outbox.getId(), ex);
		}
	}
}
