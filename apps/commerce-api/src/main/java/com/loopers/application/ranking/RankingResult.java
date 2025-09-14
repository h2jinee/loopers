package com.loopers.application.ranking;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.ranking.RankingInfo;
import lombok.Builder;

@Builder
public record RankingResult(
    Integer rank,
    Long productId,
    String productName,
    Long price,
    Long brandId,
    Double score
) {
    public static RankingResult from(RankingInfo ranking, ProductInfo product) {
        if (product == null) {
            return RankingResult.builder()
                .rank(ranking.rank())
                .productId(ranking.productId())
                .productName("알 수 없는 상품")
                .price(0L)
                .brandId(0L)
                .score(ranking.score())
                .build();
        }

        return RankingResult.builder()
            .rank(ranking.rank())
            .productId(product.productId())
            .productName(product.nameKo())
            .price(product.price().amount().longValue())
            .brandId(product.brandId())
            .score(ranking.score())
            .build();
    }
}
