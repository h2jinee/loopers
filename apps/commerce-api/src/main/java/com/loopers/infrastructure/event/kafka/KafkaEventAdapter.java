package com.loopers.infrastructure.event.kafka;

import com.loopers.application.like.LikeAdded;
import com.loopers.application.like.LikeRemoved;
import com.loopers.application.order.OrderCancelled;
import com.loopers.application.order.OrderConfirmed;
import com.loopers.application.event.order.OrderEvent;
import com.loopers.application.stock.StockChanged;
import com.loopers.kafka.EventTypes;
import com.loopers.kafka.KafkaTopics;
import com.loopers.kafka.message.KafkaEventMessage;
import com.loopers.kafka.message.payload.CatalogEventPayload;
import com.loopers.kafka.message.payload.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.Collectors;

/**
 * Spring ApplicationEvent를 Kafka 이벤트로 변환하는 어댑터
 *
 * @TransactionalEventListener를 사용해서 DB 트랜잭션이 성공적으로 커밋된 후에만 Kafka로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventAdapter {

    private final KafkaEventPublisher kafkaEventPublisher;

    // ==================== Like Events ====================

    /**
     * 좋아요 추가 이벤트 처리 DB에 좋아요 저장 성공 → Kafka로 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeAdded(LikeAdded event) {
        log.info("좋아요 추가 이벤트 처리 - productId: {}, userId: {}", event.productId(), event.userId());

        // 1. 도메인 이벤트 → Kafka 페이로드 변환
        CatalogEventPayload.LikeAdded payload = CatalogEventPayload.LikeAdded.builder().productId(event.productId())
            .userId(event.userId()).addedAt(event.addedAt()).build();

        // 2. Kafka 메시지 생성
        KafkaEventMessage<CatalogEventPayload.LikeAdded> message =
            KafkaEventMessage.of(
                EventTypes.LIKE_ADDED,
                String.valueOf(event.productId()),
                payload
            );

        // 3. Kafka로 발행 (productId를 key로 사용 = 같은 상품은 순서 보장)
        kafkaEventPublisher.publish(KafkaTopics.CATALOG_EVENTS, String.valueOf(event.productId()),  // partition key
            message);
    }

    /**
     * 좋아요 제거 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeRemoved(LikeRemoved event) {
        log.info("좋아요 제거 이벤트 처리 - productId: {}, userId: {}", event.productId(), event.userId());

        CatalogEventPayload.LikeRemoved payload = CatalogEventPayload.LikeRemoved.builder().productId(event.productId())
            .userId(event.userId()).removedAt(event.removedAt()).build();

        KafkaEventMessage<CatalogEventPayload.LikeRemoved> message =
            KafkaEventMessage.of(
                EventTypes.LIKE_REMOVED,
                String.valueOf(event.productId()),
                payload
            );

        kafkaEventPublisher.publish(KafkaTopics.CATALOG_EVENTS, String.valueOf(event.productId()), message);
    }

    // ==================== Order Events ====================

    /**
     * 주문 생성 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderEvent.Created event) {
        log.info("주문 생성 이벤트 처리 - orderId: {}, userId: {}", event.orderId(), event.userId());

        // OrderEvent의 OrderItemSnapshot → OrderEventPayload.OrderItem 변환
        var items = event.orderItems().stream().map(
            item -> OrderEventPayload.OrderItem.builder().productId(item.productId()).quantity(item.quantity())
                .price(null)  // OrderItemSnapshot에 price 없으면 null
                .build()).collect(Collectors.toList());

        OrderEventPayload.OrderCreated payload = OrderEventPayload.OrderCreated.builder().orderId(event.orderId())
            .userId(event.userId()).totalAmount(event.totalAmount()).items(items).createdAt(event.createdAt()).build();

        KafkaEventMessage<OrderEventPayload.OrderCreated> message =
            KafkaEventMessage.of(
                EventTypes.ORDER_CREATED,
                String.valueOf(event.orderId()),
                payload
            );

        // orderId를 key로 사용 = 같은 주문은 순서 보장
        kafkaEventPublisher.publish(KafkaTopics.ORDER_EVENTS, String.valueOf(event.orderId()), message);
    }

    /**
     * 주문 확정 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmed event) {
        log.info("주문 확정 이벤트 처리 - orderId: {}", event.orderId());

        OrderEventPayload.OrderConfirmed payload = OrderEventPayload.OrderConfirmed.builder().orderId(event.orderId())
            .userId(event.userId()).confirmedAt(event.confirmedAt()).build();

        KafkaEventMessage<OrderEventPayload.OrderConfirmed> message =
            KafkaEventMessage.of(
                EventTypes.ORDER_CONFIRMED,
                String.valueOf(event.orderId()),
                payload
            );

        kafkaEventPublisher.publish(KafkaTopics.ORDER_EVENTS, String.valueOf(event.orderId()), message);
    }

    /**
     * 주문 취소 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelled event) {
        log.info("주문 취소 이벤트 처리 - orderId: {}, reason: {}", event.orderId(), event.reason());

        OrderEventPayload.OrderCancelled payload = OrderEventPayload.OrderCancelled.builder().orderId(event.orderId())
            .userId(event.userId()).reason(event.reason()).cancelledAt(event.cancelledAt()).build();

        KafkaEventMessage<OrderEventPayload.OrderCancelled> message =
            KafkaEventMessage.of(
                EventTypes.ORDER_CANCELLED,
                String.valueOf(event.orderId()),
                payload
            );

        kafkaEventPublisher.publish(KafkaTopics.ORDER_EVENTS, String.valueOf(event.orderId()), message);
    }
    
    // ==================== Stock Events ====================
    
    /**
     * 재고 변경 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockChanged(StockChanged event) {
        log.info("재고 변경 이벤트 처리 - productId: {}, previousQty: {}, currentQty: {}, reason: {}",
            event.productId(), event.previousQuantity(), event.currentQuantity(), event.changeReason());

        CatalogEventPayload.StockChanged payload = CatalogEventPayload.StockChanged.builder()
            .productId(event.productId())
            .previousQuantity(event.previousQuantity())
            .currentQuantity(event.currentQuantity())
            .changeReason(event.changeReason())
            .changedAt(event.changedAt())
            .build();

        KafkaEventMessage<CatalogEventPayload.StockChanged> message =
            KafkaEventMessage.of(
                EventTypes.STOCK_CHANGED,
                String.valueOf(event.productId()),
                payload
            );

        kafkaEventPublisher.publish(KafkaTopics.CATALOG_EVENTS, String.valueOf(event.productId()), message);
    }
}
