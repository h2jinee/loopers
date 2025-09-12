package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    // 캐시 키 전략
    private static final String PRODUCT_KEY = "product:";
    private static final String LIST_KEY = "product:list:";
    private static final String POPULAR_KEY = "product:popular:";
    private static final long CACHE_TTL = 5;

    private final ProductJpaRepository jpaRepository;
    private final RedisProductCache cache;  // Redis 작업 위임

    @Override
    public Optional<Product> findById(Long id) {
        String key = PRODUCT_KEY + id;

        // 1. 캐시 조회
        Optional<ProductCacheDocument> cached = cache.get(key, ProductCacheDocument.class);
        if (cached.isPresent()) {
            // 캐시 히트 시 ID로 실제 엔티티 조회
            return jpaRepository.findById(id);
        }

        // 2. DB 조회
        Optional<Product> product = jpaRepository.findById(id);

        // 3. 캐시 저장
        product.ifPresent(p ->
            cache.set(key, ProductCacheDocument.from(p), CACHE_TTL)
        );

        return product;
    }

    @Override
    public Page<Product> findAllWithLikeCount(Pageable pageable) {
        String key = buildPopularKey(pageable);

        // 1. 캐시 조회
        Optional<PageCacheDocument> cached = cache.get(key, PageCacheDocument.class);
        if (cached.isPresent()) {
            // ID 목록으로 실제 엔티티 재조회
            List<Long> ids = cached.get().content().stream()
                .map(ProductCacheDocument::id)
                .toList();
            List<Product> products = jpaRepository.findAllById(ids);
            return new PageImpl<>(products, pageable, cached.get().totalElements());
        }

        // 2. DB 조회
        Page<Product> page = jpaRepository.findAllByOrderByLikeCountDesc(pageable);

        // 3. 캐시 저장
        cache.set(key, PageCacheDocument.from(page), CACHE_TTL);

        return page;
    }

    @Override
    public void incrementLikeCount(Long productId) {
        jpaRepository.incrementLikeCount(productId);
        evictProductCaches(productId);
    }

    private void evictProductCaches(Long productId) {
        // 상품 상세 캐시 삭제
        cache.evict(PRODUCT_KEY + productId);

        // 목록 캐시 삭제 (패턴 매칭)
        cache.evictByPattern("product:list:.*");
        cache.evictByPattern("product:popular:.*");
    }

    private String buildPopularKey(Pageable pageable) {
        return POPULAR_KEY +
            pageable.getPageNumber() + ":" +
            pageable.getPageSize() + ":" +
            pageable.getSort();
    }

    @Override
    public Page<Product> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        String key = LIST_KEY + "brand:" + brandId + ":" +
            pageable.getPageNumber() + ":" + pageable.getPageSize();

        // 캐시 조회
        Optional<PageCacheDocument> cached = cache.get(key, PageCacheDocument.class);
        if (cached.isPresent()) {
            List<Long> ids = cached.get().content().stream()
                .map(ProductCacheDocument::id)
                .toList();
            List<Product> products = jpaRepository.findAllById(ids);
            return new PageImpl<>(products, pageable, cached.get().totalElements());
        }

        // DB 조회
        Page<Product> page = jpaRepository.findByBrandIdOrderByLikeCountDesc(brandId, pageable);

        // 캐시 저장
        cache.set(key, PageCacheDocument.from(page), CACHE_TTL);

        return page;
    }

    @Override
    public List<Product> findByIdIn(List<Long> productIds) {
        return jpaRepository.findAllById(productIds);
    }

    @Override
    public Product save(Product product) {
        Product saved = jpaRepository.save(product);
        evictProductCaches(saved.getId());
        return saved;
    }

    @Override
    public void decrementLikeCount(Long productId) {
        jpaRepository.decrementLikeCount(productId);
        evictProductCaches(productId);
    }
}
