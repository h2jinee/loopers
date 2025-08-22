package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.domain.payment.vo.CardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PgPaymentPort pgPaymentPort;
    
    /**
     * PG 결제 처리
     */
    public Payment processPgPayment(
        Long orderId,
        String userId,
        Money amount,
        CardInfo cardInfo
    ) {
        PgPaymentCommand command = new PgPaymentCommand(
            orderId,
            userId,
            amount,
            cardInfo
        );
        
        PgPaymentResult result = pgPaymentPort.processPayment(command);
        
        Payment payment = Payment.createPgPayment(
            orderId,
            userId,
            amount,
            result.transactionId()
        );
        
        if (result.isSuccess()) {
            payment.complete();
            log.info("PG 결제 완료: orderId={}, transactionId={}", orderId, result.transactionId());
        } else {
            payment.fail(result.failureReason());
            log.warn("PG 결제 실패: orderId={}, reason={}", orderId, result.failureReason());
        }
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 내역 저장
     */
    public void savePaymentHistory(Long orderId, PaymentResult result) {
        Payment payment = convertToPayment(orderId, result);
        paymentRepository.save(payment);
    }
    
    private Payment convertToPayment(Long orderId, PaymentResult result) {
        Payment payment;
        
        if (result.method() == PaymentMethod.POINT) {
            payment = Payment.createPointPayment(orderId, result.userId(), result.amount());
        } else if (result.method() == PaymentMethod.PG) {
            payment = Payment.createPgPayment(orderId, result.userId(), result.amount(), result.transactionId());
        } else {
            throw new IllegalArgumentException("COMBINED 타입은 개별 결제로 처리되어야 합니다.");
        }
        
        if (result.isSuccess()) {
            payment.complete();
        } else if (result.status() == PaymentResultStatus.FAILED) {
            payment.fail(result.message());
        }
        
        return payment;
    }
    
    /**
     * 결제 완료 처리 (콜백용)
     */
    public void completePayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + transactionId));
        
        payment.complete();
        paymentRepository.save(payment);
        log.info("결제 완료 처리: transactionId={}", transactionId);
    }
    
    /**
     * 결제 실패 처리 (콜백용)
     */
    public void failPayment(String transactionId, String reason) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + transactionId));
        
        payment.fail(reason);
        paymentRepository.save(payment);
        log.warn("결제 실패 처리: transactionId={}, reason={}", transactionId, reason);
    }
    
}
