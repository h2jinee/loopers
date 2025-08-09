package com.loopers.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductWithBrandDto;
import com.loopers.domain.product.ProductWithLikeCountDto;
import com.loopers.domain.product.ProductStockInfo;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithLikeCountDto(
            p, pc.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN ProductCountEntity pc ON p.id = pc.productId
        """)
    Page<ProductWithLikeCountDto> findAllWithLikeCountOptimized(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithLikeCountDto(
            p, pc.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN ProductCountEntity pc ON p.id = pc.productId
        WHERE p.brandId = :brandId
        """)
    Page<ProductWithLikeCountDto> findByBrandIdWithLikeCountOptimized(@Param("brandId") Long brandId, Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, pc.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON p.brandId = b.id
        LEFT JOIN ProductCountEntity pc ON p.id = pc.productId
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findAllProductsWithBrand(Pageable pageable);
    
    @Query("""
        SELECT new com.loopers.domain.product.ProductWithBrandDto(
            p, b, pc.likeCount
        )
        FROM ProductEntity p
        LEFT JOIN BrandEntity b ON p.brandId = b.id
        LEFT JOIN ProductCountEntity pc ON p.id = pc.productId
        WHERE p.brandId = :brandId
        ORDER BY p.createdAt DESC
        """)
    Page<ProductWithBrandDto> findProductsWithBrandByBrandId(@Param("brandId") Long brandId, Pageable pageable);
    
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
