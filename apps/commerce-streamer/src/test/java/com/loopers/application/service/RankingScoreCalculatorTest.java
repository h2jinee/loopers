package com.loopers.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class RankingScoreCalculatorTest {

    private RankingScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RankingScoreCalculator();
        // 테스트용 가중치 설정 (리플렉션 사용)
        ReflectionTestUtils.setField(calculator, "viewWeight", 0.1);
        ReflectionTestUtils.setField(calculator, "likeWeight", 0.2);
        ReflectionTestUtils.setField(calculator, "orderWeight", 0.6);
    }

    @DisplayName("조회 점수 계산")
    @Nested
    class calculateViewScore {

        @Test
        @DisplayName("조회 1회는 0.1점을 반환한다")
        void returnsCorrectScore_whenViewOccurs() {
            // When
            double score = calculator.calculateViewScore();

            // Then
            assertThat(score).isEqualTo(0.1);
        }
    }

    @DisplayName("좋아요 점수 계산")
    @Nested
    class calculateLikeScore {

        @Test
        @DisplayName("좋아요 1개는 0.2점을 반환한다")
        void returnsCorrectScore_whenLikeOccurs() {
            // When
            double score = calculator.calculateLikeScore();

            // Then
            assertThat(score).isEqualTo(0.2);
        }
    }

    @DisplayName("주문 점수 계산")
    @Nested
    class calculateOrderScore {

        @Test
        @DisplayName("10,000원 상품 1개 주문 시 로그 스케일이 적용된 점수를 반환한다")
        void returnsLogScaledScore_whenOrderOccurs() {
            // Given
            long price = 10000L;
            int quantity = 1;

            // When
            double score = calculator.calculateOrderScore(price, quantity);

            // Then
            // log10(10000 + 1) = 4.0 * 0.6 = 2.4
            double expectedScore = Math.log10(10001) * 0.6;
            assertThat(score).isCloseTo(expectedScore, within(0.01));
        }

        @Test
        @DisplayName("100,000원 상품 2개 주문 시 정확한 점수를 계산한다")
        void calculatesCorrectScore_forMultipleQuantity() {
            // Given
            long price = 100000L;
            int quantity = 2;

            // When
            double score = calculator.calculateOrderScore(price, quantity);

            // Then
            // log10(200000 + 1) = 5.3 * 0.6 = 3.18
            double expectedScore = Math.log10(200001) * 0.6;
            assertThat(score).isCloseTo(expectedScore, within(0.01));
        }

        @Test
        @DisplayName("0원 상품도 최소 점수를 반환한다")
        void returnsMinimumScore_whenPriceIsZero() {
            // Given
            long price = 0L;
            int quantity = 1;

            // When
            double score = calculator.calculateOrderScore(price, quantity);

            // Then
            // log10(0 + 1) = 0 * 0.6 = 0
            assertThat(score).isEqualTo(0.0);
        }

        @Test
        @DisplayName("로그 스케일로 인해 고가 상품의 점수 증가율이 둔화된다")
        void demonstratesLogScaleEffect() {
            // 같은 금액(1만원)씩 증가시킬 때
            double score_1000 = calculator.calculateOrderScore(1000, 1);
            double score_11000 = calculator.calculateOrderScore(11000, 1);
            double score_100000 = calculator.calculateOrderScore(100000, 1);
            double score_110000 = calculator.calculateOrderScore(110000, 1);

            double lowPriceIncrease = score_11000 - score_1000;     // 저가 상품 1만원 증가
            double highPriceIncrease = score_110000 - score_100000; // 고가 상품 1만원 증가

            // 같은 금액 증가해도 고가일수록 점수 증가폭이 작음
            assertThat(lowPriceIncrease).isGreaterThan(highPriceIncrease);
        }
    }

    @DisplayName("가중치 변경 테스트")
    @Nested
    class weightChangeTest {

        @Test
        @DisplayName("가중치를 변경하면 점수 계산에 반영된다")
        void reflectsWeightChange_whenWeightModified() {
            // Given - 가중치 변경
            ReflectionTestUtils.setField(calculator, "viewWeight", 0.5);
            ReflectionTestUtils.setField(calculator, "likeWeight", 0.3);
            ReflectionTestUtils.setField(calculator, "orderWeight", 0.2);

            // When
            double viewScore = calculator.calculateViewScore();
            double likeScore = calculator.calculateLikeScore();
            double orderScore = calculator.calculateOrderScore(10000, 1);

            // Then
            assertThat(viewScore).isEqualTo(0.5);
            assertThat(likeScore).isEqualTo(0.3);
            assertThat(orderScore).isCloseTo(Math.log10(10001) * 0.2, within(0.01));
        }
    }
}
