package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisProductCache {

    private static final String CACHE_KEYS_SET = "cache:keys:product";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 캐시 저장 (단일 객체)
     */
    public void set(String key, Object value, long ttlMinutes) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
            registerKey(key);
        } catch (JsonProcessingException e) {
            log.error("캐시 직렬화 실패 - key: {}", key, e);
        }
    }

    /**
     * 캐시 조회
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            log.error("캐시 역직렬화 실패 - key: {}", key, e);
            evict(key); // 깨진 캐시 삭제
            return Optional.empty();
        }
    }

    /**
     * 캐시 삭제
     */
    public void evict(String key) {
        redisTemplate.delete(key);
        unregisterKey(key);
    }

    /**
     * 패턴으로 캐시 삭제 (SET 활용)
     */
    public void evictByPattern(String pattern) {
        Set<String> keys = getRegisteredKeys();
        if (keys == null || keys.isEmpty()) {
            return;
        }

        Set<String> keysToDelete = keys.stream()
            .filter(key -> key.matches(pattern))
            .collect(Collectors.toSet());

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            unregisterKeys(keysToDelete);
            log.debug("캐시 삭제 - pattern: {}, count: {}", pattern, keysToDelete.size());
        }
    }

    /**
     * 캐시 존재 여부
     */
    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    // Private 헬퍼 메서드들
    private void registerKey(String key) {
        redisTemplate.opsForSet().add(CACHE_KEYS_SET, key);
    }

    private void unregisterKey(String key) {
        redisTemplate.opsForSet().remove(CACHE_KEYS_SET, key);
    }

    private void unregisterKeys(Set<String> keys) {
        redisTemplate.opsForSet().remove(CACHE_KEYS_SET, keys.toArray());
    }

    private Set<String> getRegisteredKeys() {
        return redisTemplate.opsForSet().members(CACHE_KEYS_SET);
    }
}
