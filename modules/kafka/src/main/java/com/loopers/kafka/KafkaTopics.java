package com.loopers.kafka;

/**
 * Kafka 토픽 이름 상수
 * Producer와 Consumer가 공통으로 사용
 */
public class KafkaTopics {
	public static final String CATALOG_EVENTS = "catalog-events";
	public static final String ORDER_EVENTS = "order-events";
	public static final String DLQ_TOPIC = "dead-letter-queue";

	private KafkaTopics() {
		throw new IllegalStateException("Constants class");
	}
}
