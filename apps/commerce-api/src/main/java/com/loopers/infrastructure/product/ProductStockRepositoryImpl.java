package com.loopers.infrastructure.product;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.loopers.domain.product.ProductStockEntity;
import com.loopers.domain.product.ProductStockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductStockRepositoryImpl implements ProductStockRepository {

	private final ProductStockJpaRepository productStockJpaRepository;

	@Override
	public ProductStockEntity save(ProductStockEntity productStock) {
		return productStockJpaRepository.save(productStock);
	}

	@Override
	public Optional<ProductStockEntity> findByProductId(Long productId) {
		return productStockJpaRepository.findByProductId(productId);
	}

	@Override
	public Optional<ProductStockEntity> findByProductIdWithLock(Long productId) {
		return productStockJpaRepository.findByProductIdWithLock(productId);
	}
}
