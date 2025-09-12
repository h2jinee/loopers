package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LikeJpaRepository extends JpaRepository<Like, Long> {
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    Long countByProductId(Long productId);
    
    Page<Like> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Optional<Like> findByUserIdAndProductId(String userId, Long productId);
}
