package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductWithBrandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String NULL_VALUE = "__NULL__";
    private static final long CACHE_TTL_MINUTES = 5;
    
    private final ProductJpaRepository jpaRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public Optional<Product> findById(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        
        try {
            String cached = redisTemplate.opsForValue().get(key);
            
            if (NULL_VALUE.equals(cached)) {
                return Optional.empty();
            }
            
            if (cached != null) {
                return jpaRepository.findById(id);
            }
            
            Optional<Product> product = jpaRepository.findById(id);
            
            if (product.isEmpty()) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                return Optional.empty();
            }
            
            ProductCacheDto cacheDto = ProductCacheDto.from(product.get());
            String serialized = objectMapper.writeValueAsString(cacheDto);
            redisTemplate.opsForValue().set(key, serialized, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            
            return product;
            
        } catch (JsonProcessingException e) {
            log.error("캐시 처리 실패: productId={}", id, e);
            return jpaRepository.findById(id);
        }
    }
    
    @Override
    public Page<Product> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        return jpaRepository.findByBrandIdOrderByLikeCountDesc(brandId, pageable);
    }
    
    @Override
    public Page<Product> findAllWithLikeCount(Pageable pageable) {
        return jpaRepository.findAllByOrderByLikeCountDesc(pageable);
    }
    
    @Override
    public Product save(Product product) {
        Product saved = jpaRepository.save(product);
        evictCache(saved.getId());
        return saved;
    }
    
    @Override
    public void incrementLikeCount(Long productId) {
        jpaRepository.incrementLikeCount(productId);
        evictCache(productId);
    }
    
    @Override
    public void decrementLikeCount(Long productId) {
        jpaRepository.decrementLikeCount(productId);
        evictCache(productId);
    }
    
    @Override
    public List<Product> findByIdIn(List<Long> productIds) {
        return jpaRepository.findAllById(productIds);
    }
    
    @Override
    public boolean existsById(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key)) || jpaRepository.existsById(id);
    }
    
    public void evictCache(Long productId) {
        String key = PRODUCT_KEY_PREFIX + productId;
        redisTemplate.delete(key);
    }
}
