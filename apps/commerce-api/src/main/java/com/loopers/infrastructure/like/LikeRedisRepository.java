package com.loopers.infrastructure.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_SET_KEY = "likes:product:";  // 상품별 좋아요 Set

    /**
     * 좋아요 추가
     */
    public boolean addLike(String userId, Long productId) {
        String key = LIKE_SET_KEY + productId;

        // Redis SET에 추가 (중복 자동 방지)
        Long added = redisTemplate.opsForSet().add(key, userId);

        boolean success = added != null && added > 0;
        log.debug("Redis 좋아요 추가 시도: userId={}, productId={}, success={}", userId, productId, success);
        return success;
    }

    /**
     * 좋아요 제거
     */
    public boolean removeLike(String userId, Long productId) {
        String key = LIKE_SET_KEY + productId;

        Long removed = redisTemplate.opsForSet().remove(key, userId);

        boolean success = removed != null && removed > 0;
        log.debug("Redis 좋아요 제거 시도: userId={}, productId={}, success={}", userId, productId, success);
        return success;
    }

    /**
     * 좋아요 여부 확인
     */
    public boolean exists(String userId, Long productId) {
        String key = LIKE_SET_KEY + productId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId));
    }
}
