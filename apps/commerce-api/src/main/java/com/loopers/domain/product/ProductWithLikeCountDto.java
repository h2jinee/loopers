package com.loopers.domain.product;

public record ProductWithLikeCountDto(ProductEntity product, Long likeCount) {
	public ProductWithLikeCountDto(ProductEntity product, Long likeCount) {
		this.product = product;
		this.likeCount = likeCount != null ? likeCount : 0L;
		// 상품 엔티티에 좋아요 수 설정
		this.product.setLikeCount(this.likeCount);
	}
}
