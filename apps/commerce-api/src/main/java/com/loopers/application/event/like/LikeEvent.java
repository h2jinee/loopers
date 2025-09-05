package com.loopers.application.event.like;

import com.loopers.application.event.Event;
import java.time.LocalDateTime;

/**
 * 좋아요 도메인 이벤트
 */
public class LikeEvent {

	/**
	 * 좋아요 추가 이벤트
	 */
	public record Added(
		String userId,
		Long productId,
		LocalDateTime addedAt
	) implements Event {

		public static Added from(String userId, Long productId) {
			return new Added(userId, productId, LocalDateTime.now());
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return addedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(productId);
		}
	}

	/**
	 * 좋아요 제거 이벤트
	 */
	public record Removed(
		String userId,
		Long productId,
		LocalDateTime removedAt
	) implements Event {

		public static Removed from(String userId, Long productId) {
			return new Removed(userId, productId, LocalDateTime.now());
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return removedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(productId);
		}
	}
}
