package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.vo.ProductStatus;

public class ProductEntityTest {

    /*
     * Product 단위 테스트
     * - [x] AVAILABLE 상태면 구매 가능하다.
     * - [x] DISCONTINUED 상태면 구매 불가능하다.
     * - [x] OUT_OF_STOCK 상태면 구매 불가능하다.
     */
    @DisplayName("Product 확인 시")
    @Nested
    class Availability {
        private ProductEntity product;

        @BeforeEach
        void setUp() {
            product = new ProductEntity(
                1L, "테스트 상품", Money.of(10000), "설명",
                ProductStatus.AVAILABLE, 2024, Money.of(3000)
            );
        }

        @DisplayName("AVAILABLE 상태면 구매 가능하다.")
        @Test
        void isAvailable_whenStatusIsAvailable() {
            // assert
            assertThat(product.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        }

        @DisplayName("DISCONTINUED 상태면 구매 불가능하다.")
        @Test
        void notAvailable_whenStatusIsDiscontinued() {
            // arrange
            ProductEntity discontinuedProduct = new ProductEntity(
                1L, "단종 상품", Money.of(10000), "설명",
                ProductStatus.DISCONTINUED, 2024, Money.of(3000)
            );

            // assert
            assertThat(discontinuedProduct.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @DisplayName("OUT_OF_STOCK 상태면 구매 불가능하다.")
        @Test
        void notAvailable_whenStatusIsOutOfStock() {
            // arrange
            ProductEntity outOfStockProduct = new ProductEntity(
                1L, "품절 상품", Money.of(10000), "설명",
                ProductStatus.OUT_OF_STOCK, 2024, Money.of(3000)
            );

            // assert
            assertThat(outOfStockProduct.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        }
    }
}
