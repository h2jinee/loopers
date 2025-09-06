package com.loopers.interfaces.consumer.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqPublisher {

	private final KafkaTemplate<Object, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 실패한 메시지를 DLQ로 전송
	 */
	public void sendToDlq(String originalTopic, String originalMessage,
		String consumerName, String errorMessage) {
		try {
			Map<String, Object> dlqMessage = new HashMap<>();
			dlqMessage.put("originalTopic", originalTopic);
			dlqMessage.put("originalMessage", originalMessage);
			dlqMessage.put("consumerName", consumerName);
			dlqMessage.put("errorMessage", errorMessage);
            dlqMessage.put("failedAt", LocalDateTime.now().toString());
			dlqMessage.put("retryCount", 0);

			String dlqJson = objectMapper.writeValueAsString(dlqMessage);

            kafkaTemplate.send(KafkaTopics.DLQ_TOPIC, dlqJson).get();

            log.warn("메시지를 DLQ로 전송 - originalTopic: {}, consumer: {}, error: {}",
				originalTopic, consumerName, errorMessage);

		} catch (Exception e) {
			log.error("DLQ 전송 실패", e);
		}
	}
}
