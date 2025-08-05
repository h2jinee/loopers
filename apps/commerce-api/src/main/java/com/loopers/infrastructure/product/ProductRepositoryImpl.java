package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductCountRepository;
import com.loopers.domain.product.ProductCountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    
    // TODO DB 추가 시 Map -> DB 변경해야 함.
    private final Map<Long, ProductEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ProductCountRepository productCountRepository;
    
    @Override
    public ProductEntity save(ProductEntity product) {
        if (product.getId() == null || product.getId() == 0L) {
            // 새 상품 생성 시 prePersist 호출
            try {
                var prePersistMethod = product.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(product);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist product", e);
            }
            
            // ID 생성
            Long newId = idGenerator.getAndIncrement();
            store.put(newId, product);
            
            // 생성된 ID로 새 엔티티 반환
            return store.get(newId);
        } else {
            // 업데이트 시 preUpdate 호출
            try {
                var preUpdateMethod = product.getClass().getSuperclass().getDeclaredMethod("preUpdate");
                preUpdateMethod.setAccessible(true);
                preUpdateMethod.invoke(product);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update product", e);
            }
            
            store.put(product.getId(), product);
            return product;
        }
    }
    
    @Override
    public Optional<ProductEntity> findById(Long productId) {
        return Optional.ofNullable(store.get(productId));
    }

    @Override
    public Page<ProductEntity> findAllWithLikeCount(Pageable pageable) {
        List<ProductEntity> allProducts = new ArrayList<>(store.values());
        allProducts.forEach(product -> {
            Long likeCount = productCountRepository.findByProductId(product.getId())
                .map(ProductCountEntity::getLikeCount)
                .orElse(0L);
            product.setLikeCount(likeCount);
        });
        return createPage(allProducts, pageable, true);
    }
    
    @Override
    public Page<ProductEntity> findByBrandIdWithLikeCount(Long brandId, Pageable pageable) {
        List<ProductEntity> brandProducts = store.values().stream()
            .filter(product -> product.getBrandId().equals(brandId))
            .map(product -> {
                Long likeCount = productCountRepository.findByProductId(product.getId())
                    .map(ProductCountEntity::getLikeCount)
                    .orElse(0L);
                product.setLikeCount(likeCount);
                return product;
            })
            .collect(Collectors.toList());
        return createPage(brandProducts, pageable, true);
    }
    
    @Override
    public boolean existsById(Long productId) {
        return store.containsKey(productId);
    }
    
    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
    
    private Page<ProductEntity> createPage(List<ProductEntity> products, Pageable pageable, boolean withLikeCount) {
        // 정렬 적용
        products.sort(getComparator(pageable.getSort(), withLikeCount));
        
        // 페이징 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        
        if (start >= products.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, products.size());
        }
        
        List<ProductEntity> pageContent = products.subList(start, end);
        return new PageImpl<>(pageContent, pageable, products.size());
    }
    
    private Comparator<ProductEntity> getComparator(Sort sort, boolean withLikeCount) {
        if (sort.isEmpty()) {
            return Comparator.comparing(ProductEntity::getCreatedAt).reversed();
        }
        
        Sort.Order order = sort.iterator().next();
        String property = order.getProperty();
        
        Comparator<ProductEntity> comparator = switch (property) {
            case "createdAt" -> Comparator.comparing(ProductEntity::getCreatedAt, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "price" -> Comparator.comparing(p -> p.getPrice().amount());
            case "likeCount" -> withLikeCount ? 
                Comparator.comparing(ProductEntity::getLikeCount) : 
                Comparator.comparing(ProductEntity::getCreatedAt);
            default -> Comparator.comparing(ProductEntity::getCreatedAt);
        };
        
        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
