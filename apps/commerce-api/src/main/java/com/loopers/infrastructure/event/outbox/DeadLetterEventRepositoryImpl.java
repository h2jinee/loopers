package com.loopers.infrastructure.event.outbox;

import com.loopers.domain.event.DeadLetterEvent;
import com.loopers.domain.event.DeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeadLetterEventRepositoryImpl implements DeadLetterEventRepository {

	private final DeadLetterEventJpaRepository jpaRepository;

	@Override
	public DeadLetterEvent save(DeadLetterEvent deadLetterEvent) {
		return jpaRepository.save(deadLetterEvent);
	}

	@Override
	public boolean existsByOriginalOutboxId(Long outboxId) {
		return jpaRepository.existsByOriginalOutboxId(outboxId);
	}
}
