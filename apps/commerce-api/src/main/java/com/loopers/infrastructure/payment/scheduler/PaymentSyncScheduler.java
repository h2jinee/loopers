package com.loopers.infrastructure.payment.scheduler;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.payment.service.PaymentSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSyncScheduler {
    
    private final PaymentRepository paymentRepository;
    private final PaymentSyncService paymentSyncService;
    
    @Scheduled(fixedDelay = 60000)
    public void syncPendingPayments() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusMinutes(1);
        
        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreatedBefore(
            PaymentStatus.PENDING, 
            cutoffTime,
            PageRequest.of(0, 100)
        ).getContent();
        
        if (pendingPayments.isEmpty()) {
            return;
        }
        
        log.info("PENDING 결제 동기화 시작: {} 건", pendingPayments.size());
        
        for (Payment payment : pendingPayments) {
            try {
                paymentSyncService.syncPayment(payment);
            } catch (Exception e) {
                log.error("결제 동기화 실패: paymentId={}, orderId={}", 
                    payment.getId(), payment.getOrderId(), e);
            }
        }
    }
    
    @Scheduled(fixedDelay = 300000)
    public void cleanupOldPendingPayments() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusHours(24);
        
        List<Payment> oldPendingPayments = paymentRepository.findByStatusAndCreatedBefore(
            PaymentStatus.PENDING,
            cutoffTime,
            PageRequest.of(0, 100)
        ).getContent();
        
        for (Payment payment : oldPendingPayments) {
            try {
                paymentSyncService.failOldPayment(payment);
            } catch (Exception e) {
                log.error("오래된 결제 실패 처리 중 오류: paymentId={}, orderId={}",
                    payment.getId(), payment.getOrderId(), e);
            }
        }
    }
}
