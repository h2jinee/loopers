package com.loopers.interfaces.listener;

import com.loopers.application.like.LikeAdded;
import com.loopers.application.like.LikeRemoved;
import com.loopers.domain.product.ProductCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 좋아요 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventListener {
    
    private final ProductCountService productCountService;
    
    /**
     * 좋아요 추가 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeAdded(LikeAdded event) {
        try {
            Long newCount = productCountService.incrementLikeCountWithLock(event.productId());
            log.debug("좋아요 집계 증가 - productId: {}, count: {}", event.productId(), newCount);
        } catch (Exception e) {
            log.warn("좋아요 집계 실패 - productId: {}", event.productId());
        }
    }
    
    /**
     * 좋아요 삭제 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeRemoved(LikeRemoved event) {
        try {
            Long newCount = productCountService.decrementLikeCountWithLock(event.productId());
            log.debug("좋아요 집계 감소 - productId: {}, count: {}", event.productId(), newCount);
        } catch (Exception e) {
            log.warn("좋아요 집계 감소 실패 - productId: {}", event.productId());
        }
    }
}
