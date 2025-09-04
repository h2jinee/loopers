package com.loopers.infrastructure.payment.scheduler;

import com.loopers.infrastructure.payment.service.PaymentSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSyncScheduler {

    private final PaymentSyncService paymentSyncService;

    @Scheduled(fixedDelay = 60000)
    public void syncPendingPayments() {
        int syncedCount = paymentSyncService.syncPendingPayments();
        if (syncedCount > 0) {
            log.info("PENDING 결제 동기화 완료: {} 건", syncedCount);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupOldPendingPayments() {
        int cleanedCount = paymentSyncService.cleanupOldPendingPayments();
        if (cleanedCount > 0) {
            log.info("정리 완료: {} 건", cleanedCount);
        }
    }
}
