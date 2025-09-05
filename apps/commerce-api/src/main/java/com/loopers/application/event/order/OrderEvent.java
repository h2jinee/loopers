package com.loopers.application.event.order;

import com.loopers.application.event.Event;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 도메인 이벤트
 */
public class OrderEvent {

    /**
     * 주문 생성 이벤트
     * 주문이 생성되고 재고가 예약된 시점에 발행
     */
    public record Created(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemSnapshot> orderItems,  // 재고 복원용 스냅샷
        LocalDateTime createdAt
    ) implements Event {
        /**
         * Order 엔티티로부터 이벤트 생성
         */
        public static Created from(Order order) {
            return new Created(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount().amount(),
                order.getOrderLines().stream()
                    .map(OrderItemSnapshot::from)
                    .toList(),
                LocalDateTime.now()
            );
        }

		@Override
		public LocalDateTime getOccurredAt() {
			return createdAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(orderId);
		}
	}

    /**
     * 주문 항목 스냅샷 (재고 복원에 필요한 최소 정보)
     */
    public record OrderItemSnapshot(
        Long productId,
        Integer quantity
    ) {
        public static OrderItemSnapshot from(OrderLine orderLine) {
            return new OrderItemSnapshot(
                orderLine.getProductId(),
                orderLine.getQuantity()
            );
        }
    }

    /**
     * 주문 확정 이벤트
     */
    public record Confirmed(
        Long orderId,
        String userId,
        LocalDateTime confirmedAt
    ) implements Event {
        public static Confirmed from(Long orderId, String userId) {
            return new Confirmed(orderId, userId, LocalDateTime.now());
        }

		@Override
		public LocalDateTime getOccurredAt() {
			return confirmedAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(orderId);
		}
	}

    /**
     * 주문 취소 이벤트
     */
    public record Cancelled(
        Long orderId,
        String userId,
        String reason,  // "PAYMENT_FAILED", "CUSTOMER_REQUEST" 등
        LocalDateTime cancelledAt
    ) implements Event {
        public static Cancelled from(Long orderId, String userId, String reason) {
            return new Cancelled(orderId, userId, reason, LocalDateTime.now());
        }

        // 결제 실패로 인한 취소
        public static Cancelled fromPaymentFailure(Long orderId, String userId) {
            return from(orderId, userId, "PAYMENT_FAILED");
        }

		@Override
		public LocalDateTime getOccurredAt() {
			return cancelledAt;
		}

		@Override
		public String getAggregateId() {
			return String.valueOf(orderId);
		}
	}
}
