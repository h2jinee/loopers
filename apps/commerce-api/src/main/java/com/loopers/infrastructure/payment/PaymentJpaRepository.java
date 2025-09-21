package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionKey(String transactionKey);
    
    List<Payment> findByUserId(String userId);
    
    Page<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, ZonedDateTime dateTime, Pageable pageable);
    
    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
