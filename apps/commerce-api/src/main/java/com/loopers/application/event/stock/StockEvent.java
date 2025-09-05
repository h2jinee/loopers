package com.loopers.application.event.stock;

import com.loopers.application.event.Event;
import java.time.LocalDateTime;

/**
 * 재고 도메인 이벤트
 */
public class StockEvent {

	/**
	 * 재고 변경 이벤트
	 */
	public record Changed(
		Long productId,
		Integer previousQuantity,
		Integer currentQuantity,
		String changeReason,  // "ORDER_PAYMENT", "ORDER_CANCELLED"
		LocalDateTime changedAt
	) implements Event {

		public static Changed from(Long productId, Integer previousQuantity,
			Integer currentQuantity, String reason) {
			return new Changed(productId, previousQuantity, currentQuantity,
				reason, LocalDateTime.now());
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return changedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(productId);
		}
	}
}
