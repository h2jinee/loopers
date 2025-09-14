package com.loopers.application.ranking.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class RankingBatchAggregation {
    private final Map<Long, Double> productScores = new HashMap<>();

    /**
     * 점수 누적
     */
    public void accumulate(Long productId, double score) {
        productScores.merge(productId, score, Double::sum);
    }

    /**
     * 최종 점수 맵 반환
     */
    public Map<Long, Double> getFinalScores() {
        return new HashMap<>(productScores);
    }

    /**
     * 집계된 상품 수
     */
    public int size() {
        return productScores.size();
    }

    /**
     * 비어있는지 확인
     */
    public boolean isEmpty() {
        return productScores.isEmpty();
    }
}
