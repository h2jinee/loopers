package com.loopers.application.order;

import com.loopers.application.event.order.OrderEvent;
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
     * 주문 생성 이벤트 발행
     */
    public void publish(OrderEvent.Created event) {
        log.debug("주문 생성 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 주문 취소 이벤트 발행
     */
    public void publish(OrderEvent.Cancelled event) {
        log.debug("주문 취소 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 주문 확정 이벤트 발행
     */
    public void publish(OrderEvent.Confirmed event) {
        log.debug("주문 확정 이벤트 발행 - orderId: {}", event.orderId());
        applicationEventPublisher.publishEvent(event);
    }
}
