package com.loopers.kafka;

/**
 * Kafka 토픽 이름 상수
 * Producer와 Consumer가 공통으로 사용
 */
public class KafkaTopics {
    public static final String CATALOG_EVENTS = "catalog-events";  // 상품 관련 이벤트
    public static final String ORDER_EVENTS = "order-events";      // 주문 관련 이벤트

    // private 생성자로 인스턴스화 방지
    private KafkaTopics() {
        throw new IllegalStateException("Constants class");
    }
}
