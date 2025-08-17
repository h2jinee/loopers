package com.loopers.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductWithBrandDto;
import com.loopers.domain.product.ProductStockInfo;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    
    @Query("""
        SELECT p FROM ProductEntity p
        WHERE p.brandId = :brandId
        ORDER BY p.likeCount DESC
        """)
    Page<ProductEntity> findByBrandIdOrderByLikeCountDesc(@Param("brandId") Long brandId, Pageable pageable);
    
    Page<ProductEntity> findAllByOrderByLikeCountDesc(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, p.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON p.brandId = b.id
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, p.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON p.brandId = b.id
        WHERE p.brandId = :brandId
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(@Param("brandId") Long brandId, Pageable pageable);
    
    @Modifying
    @Query("UPDATE ProductEntity p SET p.likeCount = p.likeCount + 1 WHERE p.id = :productId")
    void incrementLikeCount(@Param("productId") Long productId);
    
    @Modifying
    @Query("UPDATE ProductEntity p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :productId")
    void decrementLikeCount(@Param("productId") Long productId);
    
    @Modifying
    @Query("""
        UPDATE ProductEntity p 
        SET p.likeCount = (
            SELECT COUNT(*) FROM LikeEntity l 
            WHERE l.productId = p.id
        )
        WHERE p.id = :productId
        """)
    void syncLikeCount(@Param("productId") Long productId);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductStockInfo(
            ps.productId, ps.stock
        )
        FROM ProductStockEntity ps
        WHERE ps.productId IN :productIds
        """)
    List<ProductStockInfo> findProductStockInfoByIds(@Param("productIds") List<Long> productIds);
    
    List<ProductEntity> findAllByIdIn(List<Long> ids);
}
