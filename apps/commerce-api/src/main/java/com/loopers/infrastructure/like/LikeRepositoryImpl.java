package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class LikeRepositoryImpl implements LikeRepository {
    
    private final Map<Long, LikeEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public LikeEntity save(LikeEntity like) {
        // 새 엔티티 저장
        if (like.getId() == null || like.getId() == 0L) {
            // prePersist 호출
            try {
                var prePersistMethod = like.getClass().getSuperclass().getDeclaredMethod("prePersist");
                prePersistMethod.setAccessible(true);
                prePersistMethod.invoke(like);
            } catch (Exception e) {
                throw new RuntimeException("Failed to persist like", e);
            }
            
            // ID 생성 및 저장
            Long newId = idGenerator.getAndIncrement();
            store.put(newId, like);
            return store.get(newId);
        }
        
        store.put(like.getId(), like);
        return like;
    }
    
    @Override
    public Page<LikeEntity> findByUserId(String userId, Pageable pageable) {
        List<LikeEntity> userLikes = store.values().stream()
            .filter(like -> like.getUserId().equals(userId))
            .filter(like -> like.getDeletedAt() == null)
            .sorted(Comparator.comparing(LikeEntity::getCreatedAt).reversed())
            .collect(Collectors.toList());
        
        return createPage(userLikes, pageable);
    }
    
    @Override
    public void deleteByUserIdAndProductId(String userId, Long productId) {
        Optional<LikeEntity> likeOpt = findByUserIdAndProductId(userId, productId);
        likeOpt.ifPresent(like -> {
            like.delete();
            store.put(like.getId(), like);
        });
    }
    
    @Override
    public boolean existsByUserIdAndProductId(String userId, Long productId) {
        return findByUserIdAndProductId(userId, productId).isPresent();
    }
    
    @Override
    public Long countByProductId(Long productId) {
        return store.values().stream()
            .filter(like -> like.getProductId().equals(productId))
            .filter(like -> like.getDeletedAt() == null)
            .count();
    }
    
    @Override
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }
    
    private Optional<LikeEntity> findByUserIdAndProductId(String userId, Long productId) {
        return store.values().stream()
            .filter(like -> like.getUserId().equals(userId))
            .filter(like -> like.getProductId().equals(productId))
            .filter(like -> like.getDeletedAt() == null)
            .findFirst();
    }
    
    private Page<LikeEntity> createPage(List<LikeEntity> likes, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), likes.size());
        
        if (start >= likes.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, likes.size());
        }
        
        List<LikeEntity> pageContent = likes.subList(start, end);
        return new PageImpl<>(pageContent, pageable, likes.size());
    }
}
