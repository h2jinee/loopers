package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikedProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeJpaRepository extends JpaRepository<Like, Long> {
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    void deleteByUserIdAndProductId(String userId, Long productId);
    
    Long countByProductId(Long productId);
    
    /**
     * 사용자가 좋아요한 상품 목록 조회
     */
    @Query("""
        SELECT new com.loopers.domain.like.LikedProductDto(
            p.id,
            p.brandId,
            b.nameKo,
            p.nameKo,
            p.description,
            p.price,
            pc.likeCount,
            p.status,
            l.createdAt
        )
        FROM Like l
        INNER JOIN Product p ON l.productId = p.id
        LEFT JOIN Brand b ON p.brandId = b.id
        LEFT JOIN ProductCount pc ON p.id = pc.productId
        WHERE l.userId = :userId
        ORDER BY l.createdAt DESC
        """)
    Page<LikedProductDto> findByUserId(
        @Param("userId") String userId, 
        Pageable pageable
    );
}
