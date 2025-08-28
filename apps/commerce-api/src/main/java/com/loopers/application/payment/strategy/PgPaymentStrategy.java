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

    private CardInfo createCardInfo(PgPaymentInfo pgInfo) {
        CardType cardType = CardType.valueOf(pgInfo.cardType());
        
        return new CardInfo(
            cardType,
            pgInfo.cardNo()
        );
    }
}
