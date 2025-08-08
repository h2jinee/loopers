package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointJpaRepository extends JpaRepository<PointEntity, String> {

}
