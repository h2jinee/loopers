package com.loopers.utils;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisCleanUp {

	private final RedisConnectionFactory redisConnectionFactory;

	public RedisCleanUp(RedisConnectionFactory redisConnectionFactory) {
		this.redisConnectionFactory = redisConnectionFactory;
	}

	public void truncateAll() {
		try (var connection = redisConnectionFactory.getConnection()) {
			connection.serverCommands().flushAll();
		}
	}
}
