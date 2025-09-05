package com.loopers.application.event;

/**
 * 이벤트 퍼블리셔 인터페이스
 */
public interface EventPublisher {

	/**
	 * 이벤트 발행
	 * @param event Event 인터페이스를 구현한 이벤트
	 */
	void publish(Event event);
}
