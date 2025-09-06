package com.loopers.infrastructure.event;

import com.loopers.domain.event.DeadLetterKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterKafkaEventJpaRepository extends JpaRepository<DeadLetterKafkaEvent, Long> {

}
