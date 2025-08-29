package com.loopers.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 주문 애플리케이션 이벤트 퍼블리셔
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApplicationEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 주문 완료 이벤트 발행
     */
    public void publish(OrderCompleted event) {
        log.debug("주문 완료 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 주문 취소 이벤트 발행
     */
    public void publish(OrderCancelled event) {
        log.debug("주문 취소 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 주문 확정 이벤트 발행
     */
    public void publish(OrderConfirmed event) {
        log.debug("주문 확정 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
}
