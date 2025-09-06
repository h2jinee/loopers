package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.DeadLetterKafkaEvent;
import com.loopers.domain.event.DeadLetterKafkaEventRepository;
import com.loopers.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Dead Letter Queue Consumer
 * 처리 실패한 메시지들을 DB에 저장하여 수동 재처리 가능하도록 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqConsumer {

	private final ObjectMapper objectMapper;
	private final DeadLetterKafkaEventRepository deadLetterRepository;

	@KafkaListener(
		topics = KafkaTopics.DLQ_TOPIC,
		groupId = "dlq-group",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(String messageJson, Acknowledgment ack) {
		try {
			log.info("DLQ 메시지 수신: {}", messageJson);

			// DLQ 메시지 파싱
            Map<String, Object> dlqData = objectMapper.readValue(
                messageJson,
                new TypeReference<>() {
                }
            );

			String originalTopic = (String) dlqData.get("originalTopic");
			String originalMessage = (String) dlqData.get("originalMessage");
			String consumerName = (String) dlqData.get("consumerName");
			String errorMessage = (String) dlqData.get("errorMessage");
			Integer retryCount = (Integer) dlqData.getOrDefault("retryCount", 0);

			// DB에 저장 (수동 재처리를 위해)
			DeadLetterKafkaEvent deadLetter = DeadLetterKafkaEvent.create(
				originalTopic,
				originalMessage,
				consumerName,
				errorMessage,
				retryCount
			);

			deadLetterRepository.save(deadLetter);

			log.warn("DLQ 메시지 DB 저장 완료 - topic: {}, consumer: {}",
				originalTopic, consumerName);

			ack.acknowledge();

		} catch (Exception e) {
			log.error("DLQ 메시지 처리 실패", e);
			// DLQ 처리도 실패하면 로그만 남기고 ACK
			ack.acknowledge();
		}
	}
}
