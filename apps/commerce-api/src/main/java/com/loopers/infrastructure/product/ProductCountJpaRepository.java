package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCountEntity;
import com.loopers.domain.product.ProductCountRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ProductCountJpaRepository extends JpaRepository<ProductCountEntity, Long>, ProductCountRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pc FROM ProductCountEntity pc WHERE pc.productId = :productId")
    Optional<ProductCountEntity> findByProductIdWithPessimisticLock(@Param("productId") Long productId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT pc FROM ProductCountEntity pc WHERE pc.productId = :productId")
    Optional<ProductCountEntity> findByProductIdWithOptimisticLock(@Param("productId") Long productId);
}
