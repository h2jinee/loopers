package com.loopers.domain.payment;

public interface PaymentStrategy {
    PaymentResult execute(PaymentCommand.Process command);
}
