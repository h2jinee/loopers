package com.loopers.infrastructure.event.outbox;

import com.loopers.domain.event.Outbox;
import com.loopers.domain.event.OutboxRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

	private final OutboxJpaRepository jpaRepository;

	@Override
	public Outbox save(Outbox outbox) {
		return jpaRepository.save(outbox);
	}

	@Override
	public List<Outbox> findByStatusOrderByCreatedAt(Outbox.OutboxStatus status, Pageable pageable) {
		return jpaRepository.findByStatusOrderByCreatedAt(status, pageable);
	}

	@Override
	public List<Outbox> findFailedEventsForRetry(int maxRetryCount, Pageable pageable) {
		return jpaRepository.findByStatusAndRetryCountLessThan(
			Outbox.OutboxStatus.FAILED, maxRetryCount, pageable);
	}
}
