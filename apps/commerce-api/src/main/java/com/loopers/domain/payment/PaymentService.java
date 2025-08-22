package com.loopers.domain.payment;

import com.loopers.domain.common.Money;
import com.loopers.domain.payment.command.PgCancelCommand;
import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.port.PgPaymentPort;
import com.loopers.domain.payment.result.PgCancelResult;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.domain.payment.vo.CardInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
     * 포인트 결제 처리
     */
    public Payment processPointPayment(
        Long orderId,
        String userId,
        Money amount
    ) {
        Payment payment = Payment.createPointPayment(orderId, userId, amount);
        payment.complete();
        
        log.info("포인트 결제 완료: orderId={}, amount={}", orderId, amount);
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
            // TODO : COMBINED의 경우 두 개의 별도 Payment 생성이 필요
            // 현재는 통합 금액으로 PG 결제로 처리 (추후 개선 필요)
            payment = Payment.createPgPayment(orderId, result.userId(), result.amount(), result.transactionId());
        }
        
        if (result.isSuccess()) {
            payment.complete();
        } else if (result.status() == PaymentResultStatus.FAILED) {
            payment.fail(result.message());
        }
        
        return payment;
    }
    
    /**
     * 결제 취소
     */
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "결제 정보를 찾을 수 없습니다. paymentId=" + paymentId
            ));
        
        if (payment.getPaymentMethod() == PaymentMethod.PG) {
            PgCancelCommand cancelCommand = new PgCancelCommand(
                payment.getTransactionId(),
                payment.getUserId(),
                payment.getAmount(),
                "사용자 요청"
            );
            
            PgCancelResult result = pgPaymentPort.cancelPayment(cancelCommand);
            
            if (!result.isSuccess()) {
                throw new CoreException(
                    ErrorType.INTERNAL_ERROR,
                    "PG 결제 취소 실패: " + result.failureReason()
                );
            }
        }
        
        payment.cancel();
        paymentRepository.save(payment);
        
        log.info("결제 취소 완료: paymentId={}", paymentId);
    }
    
}
