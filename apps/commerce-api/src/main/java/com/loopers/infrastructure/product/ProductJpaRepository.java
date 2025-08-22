package com.loopers.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductWithBrandDto;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    
    @Query("""
        SELECT p FROM Product p
        WHERE p.brandId = :brandId
        ORDER BY p.likeCount DESC
        """)
    Page<Product> findByBrandIdOrderByLikeCountDesc(@Param("brandId") Long brandId, Pageable pageable);
    
    Page<Product> findAllByOrderByLikeCountDesc(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, p.likeCount
        )
        FROM Product p
        LEFT JOIN Brand b ON p.brandId = b.id
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, p.likeCount
        )
        FROM Product p
        LEFT JOIN Brand b ON p.brandId = b.id
        WHERE p.brandId = :brandId
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(@Param("brandId") Long brandId, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Product p SET p.likeCount = p.likeCount + 1 WHERE p.id = :productId")
    void incrementLikeCount(@Param("productId") Long productId);
    
    @Modifying
    @Query("UPDATE Product p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :productId")
    void decrementLikeCount(@Param("productId") Long productId);
}
