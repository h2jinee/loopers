package com.loopers.application.service;

import com.loopers.domain.event.DeadLetterKafkaEvent;
import com.loopers.domain.event.DeadLetterKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DlqService {

    private final DeadLetterKafkaEventRepository deadLetterRepository;

    /**
     * DLQ 메시지 저장
     */
    @Transactional
    public void saveDeadLetterEvent(String originalTopic, String originalMessage,
        String consumerName, String errorMessage) {

        DeadLetterKafkaEvent deadLetter = DeadLetterKafkaEvent.create(
            originalTopic,
            originalMessage,
            consumerName,
            errorMessage,
            0  // retryCount
        );

        deadLetterRepository.save(deadLetter);

        log.warn("DLQ 메시지 DB 저장 완료 - topic: {}, consumer: {}",
            originalTopic, consumerName);
    }
}
