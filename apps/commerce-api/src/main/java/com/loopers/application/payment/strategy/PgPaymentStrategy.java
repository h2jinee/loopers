package com.loopers.application.payment.strategy;

import com.loopers.domain.payment.*;
import com.loopers.domain.payment.vo.CardInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentStrategy implements PaymentStrategy {
    
    private final PaymentService paymentService;
    
    @Override
    public PaymentResult execute(PaymentCommand.Process command) {
        if (command.pgInfo() == null) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "PG 결제 정보가 없습니다."
            );
        }
        
        try {
            CardInfo cardInfo = createCardInfo(command.pgInfo());
            
            Payment payment = paymentService.processPgPayment(
                command.orderId(),
                command.userId(),
                command.amount(),
                cardInfo
            );
            
            if (payment.isCompleted()) {
                return PaymentResult.success(
                    payment.getPaymentMethod(),
                    payment.getAmount(),
                    payment.getTransactionId(),
                    command.userId()
                );
            } else {
                return PaymentResult.failure(
                    payment.getPaymentMethod(),
                    payment.getFailureReason(),
                    command.userId()
                );
            }
            
        } catch (Exception e) {
            log.error("PG 결제 전략 실행 실패", e);
            return PaymentResult.failure(
                PaymentMethod.PG,
                e.getMessage(),
                command.userId()
            );
        }
    }
    
    @Override
    public void cancel(Payment payment) {
        if (payment.getPaymentMethod() != PaymentMethod.PG) {
            throw new CoreException(
                ErrorType.BAD_REQUEST,
                "PG 결제가 아닙니다."
            );
        }
        
        try {
            paymentService.cancelPayment(payment.getId());
            log.info("PG 결제 취소 완료: paymentId={}", payment.getId());
        } catch (Exception e) {
            log.error("PG 결제 취소 실패: paymentId={}", payment.getId(), e);
            throw new CoreException(
                ErrorType.INTERNAL_ERROR,
                "PG 결제 취소 실패: " + e.getMessage()
            );
        }
    }
    
    /**
     * PgPaymentInfo를 CardInfo로 변환
     * TODO: 실제 구현 시 cvv, expiryDate 등 추가 필요
     */
    private CardInfo createCardInfo(PgPaymentInfo pgInfo) {
        CardType cardType = determineCardType(pgInfo.cardNumber());
        
        return new CardInfo(
            cardType,
            pgInfo.cardNumber(),
            pgInfo.cardHolder(),
            pgInfo.expiryDate(),
            pgInfo.cvv()
        );
    }
    
    /**
     * 카드 번호로 카드 종류 판별
     * 실제로는 BIN 번호로 판별
     */
    private CardType determineCardType(String cardNumber) {
        if (cardNumber.startsWith("9")) {
            return CardType.SAMSUNG;
        } else if (cardNumber.startsWith("5")) {
            return CardType.KB;
        } else {
            return CardType.HYUNDAI;
        }
    }
}
