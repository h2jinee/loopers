package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public List<Payment> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}
