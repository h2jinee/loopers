package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCountEntity;
import com.loopers.domain.product.ProductCountRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProductCountRepositoryImpl implements ProductCountRepository {
    
    // TODO DB 추가 시 Map -> DB 변경해야 함.
    private final Map<Long, ProductCountEntity> store = new ConcurrentHashMap<>();
    
    @Override
    public ProductCountEntity save(ProductCountEntity productCount) {
        if (productCount.getId() == null || productCount.getId() == 0L) {
            // 새 엔티티 생성 시 prePersist 호출
            try {
                var prePersistMethod = productCount.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(productCount);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist product count", e);
            }
        } else {
            // 업데이트 시 preUpdate 호출
            try {
                var preUpdateMethod = productCount.getClass().getSuperclass().getDeclaredMethod("preUpdate");
                preUpdateMethod.setAccessible(true);
                preUpdateMethod.invoke(productCount);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update product count", e);
            }
        }
        
        store.put(productCount.getProductId(), productCount);
        return productCount;
    }
    
    @Override
    public Optional<ProductCountEntity> findByProductId(Long productId) {
        return Optional.ofNullable(store.get(productId));
    }
    
    @Override
    public void clear() {
        store.clear();
    }
}
