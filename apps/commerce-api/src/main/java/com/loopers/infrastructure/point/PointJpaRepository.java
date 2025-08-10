package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

	Optional<PointEntity> findByUserId(String userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM PointEntity p WHERE p.userId = :userId")
	Optional<PointEntity> findByIdWithPessimisticLock(@Param("userId") String userId);

	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT p FROM PointEntity p WHERE p.userId = :userId")
	Optional<PointEntity> findByIdWithOptimisticLock(@Param("userId") String userId);
}
