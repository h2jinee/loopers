package com.loopers.domain.brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BrandRepository {

    Optional<BrandEntity> findById(Long brandId);
    
    Page<BrandEntity> findAll(Pageable pageable);

    void clear();
}
