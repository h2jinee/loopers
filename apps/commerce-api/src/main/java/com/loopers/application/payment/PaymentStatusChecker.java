package com.loopers.application.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * PG 결제 상태 확인 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChecker {
    
    private final PaymentFacade paymentFacade;
    
    /**
     * 5분마다 PENDING 상태의 결제를 확인
     */
    @Scheduled(fixedDelay = 300000) // 5분마다 실행
    public void checkPendingPayments() {
        log.info("결제 상태 확인 스케줄러 시작");
        
        try {
            ZonedDateTime oneMinuteAgo = ZonedDateTime.now().minusMinutes(1);
            int updatedCount = paymentFacade.synchronizePendingPayments(oneMinuteAgo);
            log.info("결제 상태 동기화 완료: {} 건 업데이트", updatedCount);
        } catch (Exception e) {
            log.error("결제 상태 확인 중 오류 발생", e);
        }
    }
    
    /**
     * 10분마다 타임아웃된 결제를 처리
     */
    @Scheduled(fixedDelay = 600000) // 10분마다 실행
    public void handleTimeoutPayments() {
        log.info("타임아웃 결제 처리 스케줄러 시작");
        
        try {
            ZonedDateTime tenMinutesAgo = ZonedDateTime.now().minusMinutes(10);
            int timeoutCount = paymentFacade.failTimeoutPayments(tenMinutesAgo);
            log.info("타임아웃 결제 처리 완료: {} 건", timeoutCount);
        } catch (Exception e) {
            log.error("타임아웃 결제 처리 중 오류 발생", e);
        }
    }
}
