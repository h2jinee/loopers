package com.loopers.domain.event;

public interface DeadLetterKafkaEventRepository {

	DeadLetterKafkaEvent save(DeadLetterKafkaEvent deadLetterKafkaEvent);
}
