package com.loopers.infrastructure.event.outbox;

import com.loopers.domain.event.Outbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<Outbox, Long> {

	List<Outbox> findByStatusOrderByCreatedAt(Outbox.OutboxStatus status, Pageable pageable);

	List<Outbox> findByStatusAndRetryCountLessThan(
		Outbox.OutboxStatus status, Integer retryCount, Pageable pageable);
}
