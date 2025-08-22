package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeJpaRepository extends JpaRepository<Like, Long> {
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    Long countByProductId(Long productId);
    
    /**
     * 사용자가 좋아요한 목록 조회
     */
    Page<Like> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * 사용자가 좋아요한 상품 ID 목록 조회
     */
    @Query("SELECT l.productId FROM Like l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<Long> findProductIdsByUserId(@Param("userId") String userId, Pageable pageable);
}
