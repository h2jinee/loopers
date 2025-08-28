package com.loopers.domain.brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    
    Optional<Brand> findById(Long brandId);
    
    List<Brand> findByIdIn(List<Long> brandIds);
    
    Page<Brand> findAll(Pageable pageable);
    
    Brand save(Brand brand);
}
