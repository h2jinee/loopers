package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    
    Optional<BrandEntity> findById(Long brandId);
    
    List<BrandEntity> findAllById(List<Long> brandIds);
}
