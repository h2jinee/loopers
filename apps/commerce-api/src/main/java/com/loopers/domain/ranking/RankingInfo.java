package com.loopers.domain.ranking;

public record RankingInfo(
    Integer rank,
    Long productId,
    Double score
) {
    public static RankingInfo of(Integer rank, Long productId, Double score) {
        return new RankingInfo(rank, productId, score);
    }
}
