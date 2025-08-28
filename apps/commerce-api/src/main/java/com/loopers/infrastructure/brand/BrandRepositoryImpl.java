package com.loopers.infrastructure.brand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandCacheDto;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {
    
    private static final String BRAND_KEY_PREFIX = "brand:";
    private static final String NULL_VALUE = "__NULL__";
    private static final long CACHE_TTL_MINUTES = 5;
    
    private final BrandJpaRepository jpaRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public Optional<Brand> findById(Long id) {
        String key = BRAND_KEY_PREFIX + id;
        
        try {
            String cached = redisTemplate.opsForValue().get(key);
            
            if (NULL_VALUE.equals(cached)) {
                return Optional.empty();
            }
            
            if (cached != null) {
                return jpaRepository.findById(id);
            }
            
            Optional<Brand> brand = jpaRepository.findById(id);
            
            if (brand.isEmpty()) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                return Optional.empty();
            }
            
            BrandCacheDto cacheDto = BrandCacheDto.from(brand.get());
            String serialized = objectMapper.writeValueAsString(cacheDto);
            redisTemplate.opsForValue().set(key, serialized, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            
            return brand;
            
        } catch (JsonProcessingException e) {
            log.error("캐시 처리 실패: brandId={}", id, e);
            return jpaRepository.findById(id);
        }
    }
    
    @Override
    public List<Brand> findByIdIn(List<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return new ArrayList<>();
        }
        return jpaRepository.findAllById(brandIds);
    }
    
    @Override
    public Page<Brand> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public Brand save(Brand brand) {
        Brand saved = jpaRepository.save(brand);
        evictCache(saved.getId());
        return saved;
    }
    
    public void evictCache(Long brandId) {
        String key = BRAND_KEY_PREFIX + brandId;
        redisTemplate.delete(key);
    }
}
