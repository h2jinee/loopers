package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

	private static final String CONNECTION_MASTER = "redisConnectionMaster";
	public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

	private final RedisProperties redisProperties;

	public RedisConfig(RedisProperties redisProperties) {
		this.redisProperties = redisProperties;
	}

	@Primary
	@Bean
	public LettuceConnectionFactory defaultRedisConnectionFactory() {
		var database = redisProperties.database();
		var master = redisProperties.master();
		var replicas = redisProperties.replicas();

		return lettuceConnectionFactory(database, master, replicas,
			builder -> builder.readFrom(ReadFrom.REPLICA_PREFERRED));
	}

	@Qualifier(CONNECTION_MASTER)
	@Bean
	public LettuceConnectionFactory masterRedisConnectionFactory() {
		var database = redisProperties.database();
		var master = redisProperties.master();
		var replicas = redisProperties.replicas();

		return lettuceConnectionFactory(database, master, replicas,
			builder -> builder.readFrom(ReadFrom.MASTER));
	}

	@Primary
	@Bean
	public RedisTemplate<String, String> defaultRedisTemplate(
		LettuceConnectionFactory lettuceConnectionFactory) {
		return createDefaultRedisTemplate(lettuceConnectionFactory);
	}

	@Qualifier(REDIS_TEMPLATE_MASTER)
	@Bean
	public RedisTemplate<String, String> masterRedisTemplate(
		@Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory) {
		return createDefaultRedisTemplate(lettuceConnectionFactory);
	}

	private LettuceConnectionFactory lettuceConnectionFactory(
		int database,
		RedisNodeInfo master,
		List<RedisNodeInfo> replicas,
		Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer) {

		var builder = LettuceClientConfiguration.builder();

		if (customizer != null) {
			customizer.accept(builder);
		}

		var lettuceClientConfiguration = builder.build();

		var masterReplicaConfig = new RedisStaticMasterReplicaConfiguration(
			master.host(), master.port()
		);
		masterReplicaConfig.setDatabase(database);

		replicas.forEach(replica ->
			masterReplicaConfig.addNode(replica.host(), replica.port())
		);

		return new LettuceConnectionFactory(masterReplicaConfig, lettuceClientConfiguration);
	}

	private RedisTemplate<String, String> createDefaultRedisTemplate(
		LettuceConnectionFactory connectionFactory) {
		var redisTemplate = new RedisTemplate<String, String>();

		var stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(stringSerializer);
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(stringSerializer);
		redisTemplate.setConnectionFactory(connectionFactory);

		return redisTemplate;
	}
}
