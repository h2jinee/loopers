package com.loopers.application.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {
    
    private final PaymentProcessor paymentProcessor;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String RESULT_KEY_PREFIX = "payment:result:";
    private static final String RETRY_KEY_PREFIX = "payment:result:retry:";
    private static final long RESULT_TTL_MINUTES = 10;
    
    /**
     * PG 결제 결과 처리 - 비동기 처리
     * Redis 멱등성 체크와 재시도 로직 포함
     */
    @Async
    public void processPaymentResult(PaymentResultCommand command) {
        String resultKey = RESULT_KEY_PREFIX + command.transactionKey();
        String retryKey = RETRY_KEY_PREFIX + command.transactionKey();
        
        // 1. 중복 결과 처리 체크
        Boolean isProcessed = redisTemplate.opsForValue()
            .setIfAbsent(resultKey, "processing", RESULT_TTL_MINUTES, TimeUnit.MINUTES);
        
        if (Boolean.FALSE.equals(isProcessed)) {
            log.warn("중복 결제 결과 무시: transactionKey={}", command.transactionKey());
            return;
        }
        
        try {
            // 2. 결제 결과 처리 시도 (재시도 포함)
            processPaymentResultWithRetry(command, retryKey);
            
            // 3. 처리 완료 표시
            redisTemplate.opsForValue().set(resultKey, "completed", RESULT_TTL_MINUTES, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            log.error("결제 결과 처리 최종 실패: transactionKey={}", command.transactionKey(), e);
            // 실패 시 키 삭제하여 재처리 가능하도록
            redisTemplate.delete(resultKey);
            throw e;
        }
    }
    
    /**
     * 1회 재시도 로직
     */
    private void processPaymentResultWithRetry(PaymentResultCommand command, String retryKey) {
        try {
            // 첫 번째 시도
            paymentProcessor.processPaymentResult(command);
            log.info("결제 결과 처리 성공: transactionKey={}", command.transactionKey());
            
        } catch (Exception e) {
            log.warn("결제 결과 처리 실패, 재시도 예정: transactionKey={}, error={}", 
                command.transactionKey(), e.getMessage());
            
            // 재시도 카운트 확인
            String retryCount = redisTemplate.opsForValue().get(retryKey);
            if (retryCount != null && Integer.parseInt(retryCount) >= 1) {
                log.error("재시도 횟수 초과: transactionKey={}", command.transactionKey());
                throw new RuntimeException("결제 결과 처리 재시도 횟수 초과", e);
            }
            
            // 재시도 카운트 증가
            redisTemplate.opsForValue().increment(retryKey);
            redisTemplate.expire(retryKey, RESULT_TTL_MINUTES, TimeUnit.MINUTES);
            
            // 1초 대기 후 재시도
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("재시도 대기 중 인터럽트", ie);
            }
            
            try {
                paymentProcessor.processPaymentResult(command);
                log.info("결제 결과 재시도 성공: transactionKey={}", command.transactionKey());
                
            } catch (Exception retryException) {
                log.error("결제 결과 재시도 실패: transactionKey={}", command.transactionKey(), retryException);
                throw retryException;
            }
        }
    }
    
}
