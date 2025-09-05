package com.loopers.domain.event;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OutboxRepository {
	Outbox save(Outbox outbox);

	List<Outbox> findByStatusOrderByCreatedAt(Outbox.OutboxStatus status, Pageable pageable);

	List<Outbox> findFailedEventsForRetry(int maxRetryCount, Pageable pageable);
}
