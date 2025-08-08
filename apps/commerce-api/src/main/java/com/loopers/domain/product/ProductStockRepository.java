package com.loopers.domain.product;

import java.util.Optional;

public interface ProductStockRepository {
	
	Optional<ProductStockEntity> findByProductId(Long productId);
	
	Optional<ProductStockEntity> findByProductIdWithLock(Long productId);
}
