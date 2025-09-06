package com.loopers.domain.event;

public interface DeadLetterEventRepository {

	DeadLetterEvent save(DeadLetterEvent deadLetterEvent);

	boolean existsByOriginalOutboxId(Long outboxId);
}
