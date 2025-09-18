package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    
    private final PaymentJpaRepository jpaRepository;
    
    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }
    
    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Payment> findByTransactionKey(String transactionKey) {
        return jpaRepository.findByTransactionKey(transactionKey);
    }
    
    @Override
    public List<Payment> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
    
    @Override
    public Page<Payment> findByStatusAndCreatedBefore(PaymentStatus status, ZonedDateTime dateTime, Pageable pageable) {
        return jpaRepository.findByStatusAndCreatedAtBefore(status, dateTime, pageable);
    }
    
    @Override
    public boolean existsCompletedPaymentForOrder(Long orderId) {
        return jpaRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.COMPLETED);
    }
}
