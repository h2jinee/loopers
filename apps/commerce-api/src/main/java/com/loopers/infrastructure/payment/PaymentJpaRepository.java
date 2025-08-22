package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByOrderIdAndTransactionId(Long orderId, String transactionId);
    
    List<Payment> findByOrderId(Long orderId);
    
    List<Payment> findByUserId(String userId);
}
