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
            result.transactionKey()
        );
        
        if (result.isSuccess()) {
            payment.complete();
            log.info("PG 결제 완료: orderId={}, transactionKey={}", orderId, result.transactionKey());
        } else if (result.isPending()) {
            log.info("PG 결제 대기중: orderId={}, transactionKey={}, reason={}",
                orderId, result.transactionKey(), result.failureReason());
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
            payment = Payment.createPgPayment(orderId, result.userId(), result.amount(), result.transactionKey());
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
    public Payment completePayment(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + transactionKey));
        
        payment.complete();
        paymentRepository.save(payment);
        log.info("결제 완료 처리: transactionKey={}", transactionKey);
        return payment;
    }
    
    /**
     * 결제 실패 처리 (콜백용)
     */
    public void failPayment(String transactionKey, String reason) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + transactionKey));
        
        payment.fail(reason);
        paymentRepository.save(payment);
        log.warn("결제 실패 처리: transactionKey={}, reason={}", transactionKey, reason);
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
