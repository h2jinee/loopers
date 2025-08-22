package com.loopers.domain.payment;

import java.util.Optional;
import java.util.List;

public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findById(Long id);
    
    List<Payment> findByUserId(String userId);
}
