package com.loopers.interfaces.listener;

import com.loopers.application.like.LikeAdded;
import com.loopers.application.like.LikeRemoved;
import com.loopers.domain.product.ProductCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 집계 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCountEventListener {
    
    private final ProductCountService productCountService;
    
    /**
     * 좋아요 추가 이벤트 처리
     */
    @Async
    @EventListener
    @Transactional
    public void handleLikeAdded(LikeAdded event) {
        log.info("좋아요 추가 이벤트 수신 - 집계 증가 시작 - userId: {}, productId: {}", 
            event.userId(), event.productId());
        
        try {
            // 집계 증가 (비관적 락)
            Long newCount = productCountService.incrementLikeCountWithLock(event.productId());
            
            log.info("좋아요 집계 증가 성공 - productId: {}, newCount: {}", 
                event.productId(), newCount);
            
        } catch (Exception e) {
            log.error("좋아요 집계 증가 실패 - productId: {}, 좋아요는 유지됨 (부가 로직 실패)", 
                event.productId(), e);
            
            // 집계 실패해도 좋아요는 이미 성공
            // TODO: 집계 실패 알림 or 재시도 큐 추가
        }
    }
    
    /**
     * 좋아요 삭제 이벤트 처리
     */
    @Async
    @EventListener
    @Transactional
    public void handleLikeRemoved(LikeRemoved event) {
        log.info("좋아요 삭제 이벤트 수신 - 집계 감소 시작 - userId: {}, productId: {}", 
            event.userId(), event.productId());
        
        try {
            // 집계 감소 (비관적 락)
            Long newCount = productCountService.decrementLikeCountWithLock(event.productId());
            
            log.info("좋아요 집계 감소 성공 - productId: {}, newCount: {}", 
                event.productId(), newCount);
            
        } catch (Exception e) {
            log.error("좋아요 집계 감소 실패 - productId: {}, 좋아요 삭제는 유지됨 (부가 로직 실패)", 
                event.productId(), e);
            
            // 집계 실패해도 좋아요 삭제는 이미 성공
            // TODO: 집계 실패 알림 or 재시도 큐 추가
        }
    }
}
