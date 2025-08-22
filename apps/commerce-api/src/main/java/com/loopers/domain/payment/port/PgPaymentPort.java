package com.loopers.domain.payment.port;

import com.loopers.domain.payment.command.PgPaymentCommand;
import com.loopers.domain.payment.command.PgCancelCommand;
import com.loopers.domain.payment.result.PgPaymentResult;
import com.loopers.domain.payment.result.PgCancelResult;

public interface PgPaymentPort {

    PgPaymentResult processPayment(PgPaymentCommand command);

    PgCancelResult cancelPayment(PgCancelCommand command);
}
