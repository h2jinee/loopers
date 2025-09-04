package com.loopers.infrastructure.payment.service;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.OrderResponse;
import com.loopers.infrastructure.payment.dto.TransactionDetailResponse;
import com.loopers.interfaces.api.ApiResponse;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 동기화 서비스 트랜잭션 처리를 위해 Scheduler와 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSyncService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    /**
     * PENDING 상태 결제들 동기화
     */
    @Transactional
    public int syncPendingPayments() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusMinutes(1);

        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreatedBefore(PaymentStatus.PENDING, cutoffTime,
            PageRequest.of(0, 100)).getContent();

        if (pendingPayments.isEmpty()) {
            return 0;
        }

        int syncedCount = 0;
        for (Payment payment : pendingPayments) {
            try {
                syncSinglePayment(payment);
                syncedCount++;
            } catch (Exception e) {
                log.error("결제 동기화 실패: paymentId={}", payment.getId(), e);
            }
        }
        return syncedCount;
    }

    /**
     * 오래된 PENDING 결제들 정리
     */
    @Transactional
    public int cleanupOldPendingPayments() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusHours(24);

        List<Payment> oldPendingPayments = paymentRepository.findByStatusAndCreatedBefore(PaymentStatus.PENDING, cutoffTime,
            PageRequest.of(0, 100)).getContent();

        int cleanedCount = 0;
        for (Payment payment : oldPendingPayments) {
            try {
                failSinglePayment(payment);
                cleanedCount++;
            } catch (Exception e) {
                log.error("오래된 결제 처리 실패: paymentId={}", payment.getId(), e);
            }
        }
        return cleanedCount;
    }

    @Transactional
    public void syncSinglePayment(Payment payment) {
        if (!payment.getTransactionId().startsWith("TEMP_")) {
            try {
                ApiResponse<TransactionDetailResponse> response = paymentGatewayClient.getTransaction(
                    payment.getUserId(),
                    payment.getTransactionId()
                );

                if (response != null && response.data() != null) {
                    TransactionDetailResponse detail = response.data();
                    updatePaymentStatus(payment, detail);
                }
            } catch (Exception e) {
                log.warn("PG 상태 조회 실패: transactionId={}", payment.getTransactionId(), e);
            }
        } else {
            checkOrderTransactions(payment);
        }
    }

    private void updatePaymentStatus(Payment payment, TransactionDetailResponse detail) {
        String status = detail.status();

        if ("SUCCESS".equals(status) || "COMPLETED".equals(status)) {
            payment.complete();
            log.info("결제 동기화 - 완료 처리: orderId={}, transactionId={}", payment.getOrderId(), payment.getTransactionId());

        } else if ("FAILED".equals(status) || "REJECTED".equals(status)) {
            payment.fail(detail.reason() != null ? detail.reason() : "PG 결제 실패");
            log.info("결제 동기화 - 실패 처리: orderId={}, reason={}", payment.getOrderId(), detail.reason());
        }

        paymentRepository.save(payment);
    }

    private void checkOrderTransactions(Payment payment) {
        try {
            ApiResponse<OrderResponse> response = paymentGatewayClient.getTransactionsByOrder(payment.getUserId(),
                payment.getOrderId().toString());

            if (response != null
                && response.data() != null
                && response.data().transactions() != null
                && !response.data().transactions().isEmpty()
            ) {

                OrderResponse.TransactionResponse latestTransaction = response.data().transactions().getFirst();

                payment.setTransactionId(latestTransaction.transactionKey());

                // TransactionResponse를 기반으로 상태 업데이트
                if (latestTransaction.isSuccess()) {
                    payment.complete();
                    log.info("결제 완료 확인 (주문 조회): orderId={}, transactionKey={}", payment.getOrderId(),
                        latestTransaction.transactionKey());
                } else if (latestTransaction.isFailed()) {
                    payment.fail(latestTransaction.reason());
                    log.info("결제 실패 확인 (주문 조회): orderId={}, reason={}", payment.getOrderId(), latestTransaction.reason());
                }

                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            log.warn("주문별 거래 조회 실패: orderId={}", payment.getOrderId(), e);
        }
    }

    @Transactional
    public void failSinglePayment(Payment payment) {
        payment.fail("24시간 초과 - 자동 실패 처리");
        paymentRepository.save(payment);
        log.info("오래된 PENDING 결제 실패 처리: paymentId={}, orderId={}", payment.getId(), payment.getOrderId());
    }
}
