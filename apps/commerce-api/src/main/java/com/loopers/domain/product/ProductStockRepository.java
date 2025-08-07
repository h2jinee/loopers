package com.loopers.domain.product;

import java.util.Optional;

public interface ProductStockRepository {
	ProductStockEntity save(ProductStockEntity productStock);
	Optional<ProductStockEntity> findByProductId(Long productId);
	Optional<ProductStockEntity> findByProductIdWithLock(Long productId);
}
