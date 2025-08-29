package com.loopers.application.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 좋아요 애플리케이션 이벤트 퍼블리셔
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeApplicationEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 좋아요 추가 이벤트 발행
     */
    public void publish(LikeAdded event) {
        log.debug("좋아요 추가 이벤트 발행 - userId: {}, productId: {}", 
            event.userId(), event.productId());
        applicationEventPublisher.publishEvent(event);
    }
    
    /**
     * 좋아요 삭제 이벤트 발행
     */
    public void publish(LikeRemoved event) {
        log.debug("좋아요 삭제 이벤트 발행 - userId: {}, productId: {}", 
            event.userId(), event.productId());
        applicationEventPublisher.publishEvent(event);
    }
}
