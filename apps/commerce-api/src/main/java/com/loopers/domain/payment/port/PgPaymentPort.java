package com.loopers.domain.payment.port;

import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.result.PgPaymentResult;

public interface PgPaymentPort {

    PgPaymentResult processPayment(PgPaymentCommand command);
}
