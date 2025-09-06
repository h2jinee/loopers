package com.loopers.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.List;

public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findById(Long id);
    
    Optional<Payment> findByTransactionKey(String transactionKey);
    
    List<Payment> findByUserId(String userId);
    
    Page<Payment> findByStatusAndCreatedBefore(PaymentStatus status, ZonedDateTime dateTime, Pageable pageable);
}
