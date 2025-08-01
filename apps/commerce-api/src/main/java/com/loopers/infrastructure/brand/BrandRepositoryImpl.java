package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BrandRepositoryImpl implements BrandRepository {
    
    private final Map<Long, BrandEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Optional<BrandEntity> findById(Long brandId) {
        return Optional.ofNullable(store.get(brandId));
    }
    
    @Override
    public Page<BrandEntity> findAll(Pageable pageable) {
        List<BrandEntity> allBrands = new ArrayList<>(store.values());
        
        // 정렬 적용
        allBrands.sort(getComparator(pageable.getSort()));
        
        // 페이징 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allBrands.size());
        
        if (start >= allBrands.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, allBrands.size());
        }
        
        List<BrandEntity> pageContent = allBrands.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allBrands.size());
    }

    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
    
    private Comparator<BrandEntity> getComparator(Sort sort) {
        if (sort.isEmpty()) {
            return Comparator.comparing(BrandEntity::getCreatedAt).reversed();
        }
        
        Sort.Order order = sort.iterator().next();
        String property = order.getProperty();
        
        Comparator<BrandEntity> comparator = switch (property) {
            case "createdAt" -> Comparator.comparing(BrandEntity::getCreatedAt, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "nameKo" -> Comparator.comparing(BrandEntity::getNameKo);
            case "nameEn" -> Comparator.comparing(BrandEntity::getNameEn);
            default -> Comparator.comparing(BrandEntity::getCreatedAt);
        };
        
        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
