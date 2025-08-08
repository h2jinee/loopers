package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryEntity;
import com.loopers.domain.point.PointHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryEntity, Long>, PointHistoryRepository {

}
