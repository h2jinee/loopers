package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStockEntity extends BaseEntity {
	@Column(name = "product_id", nullable = false, unique = true)
	private Long productId;

	@Column(name = "stock", nullable = false)
	private Integer stock;
	
	public ProductStockEntity(Long productId, Integer stock) {
		this.productId = productId;
		this.stock = stock;
	}
	
	public static ProductStockEntity createFor(Long productId, Integer initialStock) {
		return new ProductStockEntity(productId, initialStock);
	}

	public void decrease(Integer quantity) {
		if (stock < quantity) {
			throw new CoreException(ErrorType.CONFLICT, "재고가 부족합니다");
		}
		stock -= quantity;
	}

	public void increase(Integer quantity) {
		stock += quantity;
	}
}
