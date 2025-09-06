package com.loopers.infrastructure.event;

import com.loopers.domain.event.DeadLetterKafkaEvent;
import com.loopers.domain.event.DeadLetterKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeadLetterKafkaEventRepositoryImpl implements DeadLetterKafkaEventRepository {

	private final DeadLetterKafkaEventJpaRepository jpaRepository;

	@Override
	public DeadLetterKafkaEvent save(DeadLetterKafkaEvent deadLetterKafkaEvent) {
		return jpaRepository.save(deadLetterKafkaEvent);
	}
}
