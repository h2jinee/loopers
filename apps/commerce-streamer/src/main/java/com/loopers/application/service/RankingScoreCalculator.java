package com.loopers.application.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RankingScoreCalculator {

    @Value("${ranking.weights.view:0.1}")
    private double viewWeight;

    @Value("${ranking.weights.like:0.2}")
    private double likeWeight;

    @Value("${ranking.weights.order:0.6}")
    private double orderWeight;

    public double calculateViewScore() {
        return viewWeight * 1.0;
    }

    public double calculateLikeScore() {
        return likeWeight * 1.0;
    }

    public double calculateOrderScore(long price, int quantity) {
        double rawScore = (double) price * quantity;
        return orderWeight * Math.log10(rawScore + 1);
    }
}
