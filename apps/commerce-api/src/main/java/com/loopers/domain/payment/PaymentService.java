package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.domain.payment.vo.CardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

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
        } else if (result.isPending()) {
            log.info("PG 결제 대기중: orderId={}, transactionId={}, reason={}", 
                orderId, result.transactionId(), result.failureReason());
        } else {
            payment.fail(result.failureReason());
            log.warn("PG 결제 실패: orderId={}, reason={}", orderId, result.failureReason());
        }
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 내역 저장
     */
    public Payment savePaymentHistory(Long orderId, PaymentResult result) {
        Payment payment = convertToPayment(orderId, result);
        paymentRepository.save(payment);
        return payment;
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
    public Payment completePayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + transactionId));
        
        payment.complete();
        paymentRepository.save(payment);
        log.info("결제 완료 처리: transactionId={}", transactionId);
        return payment;
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
    
    /**
     * 특정 시간 이전에 생성된 PENDING 상태의 결제 정보 조회
     */
    public List<PaymentInfo.Pending> findPendingPayments(ZonedDateTime dateTime) {
        return paymentRepository.findByStatusAndCreatedBefore(PaymentStatus.PENDING, dateTime, Pageable.unpaged())
            .stream()
            .map(PaymentInfo.Pending::from)
            .toList();
    }
    
    /**
     * 타임아웃 처리 대상 결제 정보 조회
     */
    public List<PaymentInfo.Timeout> findTimeoutPayments(ZonedDateTime dateTime) {
        return paymentRepository.findByStatusAndCreatedBefore(PaymentStatus.PENDING, dateTime, Pageable.unpaged())
            .stream()
            .map(PaymentInfo.Timeout::from)
            .toList();
    }
    
}
