package com.loopers.domain.payment;

import java.util.Optional;
import java.util.List;

public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findById(Long id);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByUserId(String userId);
}
