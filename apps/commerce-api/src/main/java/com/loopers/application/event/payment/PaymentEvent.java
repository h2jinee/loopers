package com.loopers.application.event.payment;

import com.loopers.application.event.Event;
import java.time.LocalDateTime;

/**
 * 결제 도메인 이벤트
 */
public class PaymentEvent {

	public record Completed(
		Long orderId,
		LocalDateTime completedAt
	) implements Event {

		public static Completed of(Long orderId) {
			return new Completed(orderId, LocalDateTime.now());
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return completedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(orderId);
		}
	}

	public record Failed(
		Long orderId,
		String failureReason,
		LocalDateTime failedAt
	) implements Event {

		public static Failed of(Long orderId, String reason) {
			return new Failed(orderId, reason, LocalDateTime.now());
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return failedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(orderId);
		}
	}
}
