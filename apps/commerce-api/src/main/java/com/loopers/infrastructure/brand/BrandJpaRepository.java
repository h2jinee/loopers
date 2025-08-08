package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandJpaRepository extends JpaRepository<BrandEntity, Long> {

}
