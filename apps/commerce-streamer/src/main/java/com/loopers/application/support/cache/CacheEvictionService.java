package com.loopers.application.support.cache;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionService {

    private static final String PRODUCT_DETAIL_KEY = "product:detail:";
    private static final String CACHE_KEYS_SET = "cache:keys:product";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 상품 상세 캐시 삭제
     */
    public void evictProductDetailCache(Long productId) {
        String key = PRODUCT_DETAIL_KEY + productId;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(CACHE_KEYS_SET, key);
        log.debug("상품 캐시 삭제 - productId: {}", productId);
    }

    /**
     * 모든 상품 캐시 삭제
     */
    public void evictAllProductCaches(Long productId) {
        // 1. 상품 상세 캐시 삭제
        evictProductDetailCache(productId);

        // 2. SET에서 목록 캐시 키들 가져와서 삭제
        Set<String> cacheKeys = redisTemplate.opsForSet().members(CACHE_KEYS_SET);
        if (cacheKeys != null && !cacheKeys.isEmpty()) {
            // 목록 관련 키만 필터링
            Set<String> listKeys = cacheKeys.stream()
                .filter(key -> key.startsWith("product:list") ||
                    key.startsWith("product:popular"))
                .collect(Collectors.toSet());

            if (!listKeys.isEmpty()) {
                redisTemplate.delete(listKeys);
                redisTemplate.opsForSet().remove(CACHE_KEYS_SET, listKeys.toArray());
                log.info("목록 캐시 삭제 - {} 건", listKeys.size());
            }
        }
    }
}
