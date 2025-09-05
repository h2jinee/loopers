package com.loopers.infrastructure.event.outbox;

import com.loopers.domain.event.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterEventJpaRepository extends JpaRepository<DeadLetterEvent, Long> {

	boolean existsByOriginalOutboxId(Long outboxId);
}
