package com.loopers.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTestContainersConfig {

	private static final RedisContainer REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new RedisContainer("redis:latest");
		REDIS_CONTAINER.start();

		// System properties 설정
		System.setProperty("datasource.redis.database", "0");
		System.setProperty("datasource.redis.master.host", REDIS_CONTAINER.getHost());
		System.setProperty("datasource.redis.host.port",
			String.valueOf(REDIS_CONTAINER.getFirstMappedPort()));
		System.setProperty("datasource.redis.replicas[0].host", REDIS_CONTAINER.getHost());
		System.setProperty("datasource.redis.replicas[0].port",
			String.valueOf(REDIS_CONTAINER.getFirstMappedPort()));
	}
}
