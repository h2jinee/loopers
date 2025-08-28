package com.loopers.application.payment;

public record PaymentResultCommand(
    String transactionKey,
    Long orderId,
    boolean success,
    String failureReason
) {}