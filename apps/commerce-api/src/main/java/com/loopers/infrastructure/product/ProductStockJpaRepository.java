package com.loopers.infrastructure.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loopers.domain.product.ProductStockEntity;

import jakarta.persistence.LockModeType;

public interface ProductStockJpaRepository extends JpaRepository<ProductStockEntity, Long> {

	Optional<ProductStockEntity> findByProductId(Long productId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT ps FROM ProductStockEntity ps WHERE ps.productId = :productId")
	Optional<ProductStockEntity> findByProductIdWithLock(@Param("productId") Long productId);
}
