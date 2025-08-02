package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductCountEntityTest {

    /*
     * ProductCount 생성 단위 테스트
     * - [x] 상품 ID로 ProductCount를 생성할 수 있다.
     * - [x] 생성 시 좋아요 수는 0이다.
     * - [x] 생성 시 주문 수는 0이다.
     */
    @DisplayName("ProductCount 생성 시")
    @Nested
    class Create {
        @DisplayName("상품 ID로 ProductCount를 생성할 수 있다.")
        @Test
        void createProductCount_withProductId() {
            // arrange
            Long productId = 1L;

            // act
            ProductCountEntity productCount = new ProductCountEntity(productId);

            // assert
            assertThat(productCount).isNotNull();
            assertThat(productCount.getProductId()).isEqualTo(productId);
        }

        @DisplayName("생성 시 좋아요 수는 0이다.")
        @Test
        void initialLikeCount_isZero() {
            // act
            ProductCountEntity productCount = new ProductCountEntity(1L);

            // assert
            assertThat(productCount.getLikeCount()).isEqualTo(0L);
        }

        @DisplayName("생성 시 주문 수는 0이다.")
        @Test
        void initialOrderCount_isZero() {
            // act
            ProductCountEntity productCount = new ProductCountEntity(1L);

            // assert
            assertThat(productCount.getOrderCount()).isEqualTo(0L);
        }
    }

    /*
     * 좋아요 수 업데이트 단위 테스트
     * - [x] 좋아요 수를 업데이트할 수 있다.
     */
    @DisplayName("좋아요 수 업데이트 시")
    @Nested
    class UpdateLikeCount {
        @DisplayName("좋아요 수를 업데이트할 수 있다.")
        @Test
        void updateLikeCount_successfully() {
            // arrange
            ProductCountEntity productCount = new ProductCountEntity(1L);

            // act
            productCount.updateLikeCount(10L);

            // assert
            assertThat(productCount.getLikeCount()).isEqualTo(10L);
        }
    }
}
