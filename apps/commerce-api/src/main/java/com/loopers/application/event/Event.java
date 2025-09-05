package com.loopers.application.event;

import java.time.LocalDateTime;

public interface Event {

	/**
	 * 이벤트 발생 시간
	 */
	LocalDateTime getOccurredAt();

	/**
	 * 이벤트 타입
	 */
	default String getEventType() {
		String className = this.getClass().getSimpleName();
		// 내부 클래스인 경우 처리
		if (className.contains("$")) {
			String[] parts = className.split("\\$");
			return parts[0].replace("Event", "") + parts[1];
		}
		return className;
	}

	/**
	 * Aggregate ID (Kafka Partition Key로 사용)
	 */
	String getAggregateId();

	/**
	 * 이벤트 버전 (순서 보장용)
	 */
	default Long getVersion() {
		return getOccurredAt().toEpochSecond(java.time.ZoneOffset.UTC);
	}
}
