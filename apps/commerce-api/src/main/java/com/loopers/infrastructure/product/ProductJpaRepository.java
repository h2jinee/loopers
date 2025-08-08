package com.loopers.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long>, ProductRepository {
    
    Page<ProductEntity> findByBrandId(Long brandId, Pageable pageable);
}
