package com.loopers.domain.product;

public record ProductWithLikeCountDto(Product product, Long likeCount) {
	public ProductWithLikeCountDto(Product product, Long likeCount) {
		this.product = product;
		this.likeCount = likeCount != null ? likeCount : 0L;
		// 상품 엔티티에 좋아요 수 동기화
		this.product.syncLikeCount(this.likeCount);
	}
}
