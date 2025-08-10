package com.loopers.infrastructure.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loopers.domain.product.ProductStockEntity;
import com.loopers.domain.product.ProductStockRepository;

import jakarta.persistence.LockModeType;

@Repository
public interface ProductStockJpaRepository extends JpaRepository<ProductStockEntity, Long>, ProductStockRepository {

	@Query("SELECT ps FROM ProductStockEntity ps WHERE ps.productId = :productId")
	Optional<ProductStockEntity> findByProductId(@Param("productId") Long productId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT ps FROM ProductStockEntity ps WHERE ps.productId = :productId")
	Optional<ProductStockEntity> findByProductIdWithPessimisticLock(@Param("productId") Long productId);

	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT ps FROM ProductStockEntity ps WHERE ps.productId = :productId")
	Optional<ProductStockEntity> findByProductIdWithOptimisticLock(@Param("productId") Long productId);


}
