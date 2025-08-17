package com.loopers.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(objectMapper)
            ));
        
        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 상품 상세 캐시 - 30분
        cacheConfigurations.put("products", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 상품 목록 캐시 - 10분
        cacheConfigurations.put("productList", 
            defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 브랜드별 상품 목록 캐시 - 10분
        cacheConfigurations.put("productsByBrand", 
            defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 상품과 브랜드 정보 캐시 - 30분
        cacheConfigurations.put("productsWithBrand", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware() // 트랜잭션 지원
            .build();
    }
}
