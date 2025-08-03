package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.domain.product.vo.Stock;

public class ProductEntityTest {

    /*
     * Product 단위 테스트
     * - [x] AVAILABLE 상태이고 재고가 있으면 구매 가능하다.
     * - [x] AVAILABLE 상태이지만 재고가 없으면 구매 불가능하다.
     * - [x] DISCONTINUED 상태면 재고와 관계없이 구매 불가능하다.
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
                new Stock(10), ProductStatus.AVAILABLE, 2024, Money.of(3000)
            );
        }

        @DisplayName("AVAILABLE 상태이고 재고가 있으면 구매 가능하다.")
        @Test
        void isAvailable_whenStatusIsAvailableAndHasStock() {
            // assert
            assertThat(product.isAvailable()).isTrue();
        }

        @DisplayName("AVAILABLE 상태이지만 재고가 없으면 구매 불가능하다.")
        @Test
        void notAvailable_whenStatusIsAvailableButNoStock() {
            // arrange
            for (int i = 0; i < 10; i++) {
                product.decreaseStock(1);
            }

            // assert
            assertThat(product.getStock()).isEqualTo(0);
            assertThat(product.isAvailable()).isFalse();
        }

        @DisplayName("DISCONTINUED 상태면 재고와 관계없이 구매 불가능하다.")
        @Test
        void notAvailable_whenStatusIsDiscontinued() {
            // arrange
            ProductEntity discontinuedProduct = new ProductEntity(
                1L, "단종 상품", Money.of(10000), "설명",
                new Stock(10), ProductStatus.DISCONTINUED, 2024, Money.of(3000)
            );

            // assert
            assertThat(discontinuedProduct.getStock()).isEqualTo(10);
            assertThat(discontinuedProduct.isAvailable()).isFalse();
        }

        @DisplayName("OUT_OF_STOCK 상태면 구매 불가능하다.")
        @Test
        void notAvailable_whenStatusIsOutOfStock() {
            // arrange
            ProductEntity outOfStockProduct = new ProductEntity(
                1L, "품절 상품", Money.of(10000), "설명",
                new Stock(0), ProductStatus.OUT_OF_STOCK, 2024, Money.of(3000)
            );

            // assert
            assertThat(outOfStockProduct.isAvailable()).isFalse();
        }
    }

    /*
     * Product 재고 관리 단위 테스트
     * - [x] 재고를 감소시킬 수 있다.
     * - [x] 재고가 0이 되면 상태가 OUT_OF_STOCK으로 변경된다.
     * - [x] 특정 수량의 재고가 있는지 확인할 수 있다.
     */
    @DisplayName("Product 재고 관리 시")
    @Nested
    class StockManagement {
        private ProductEntity product;

        @BeforeEach
        void setUp() {
            product = new ProductEntity(
                1L, "테스트 상품", Money.of(10000), "설명",
                new Stock(10), ProductStatus.AVAILABLE, 2024, Money.of(3000)
            );
        }

        @DisplayName("재고를 감소시킬 수 있다.")
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10})
        void decreaseStock_successfully(int quantity) {
            // arrange
            int initialStock = product.getStock();

            // act
            product.decreaseStock(quantity);

            // assert
            assertThat(product.getStock()).isEqualTo(initialStock - quantity);
        }

        @DisplayName("재고가 0이 되면 상태가 OUT_OF_STOCK으로 변경된다.")
        @Test
        void statusChangesToOutOfStock_whenStockBecomesZero() {
            // arrange
            assertThat(product.getStatus()).isEqualTo(ProductStatus.AVAILABLE);

            // act
            product.decreaseStock(10);

            // assert
            assertThat(product.getStock()).isEqualTo(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        }

        @DisplayName("특정 수량의 재고가 있는지 확인할 수 있다.")
        @Test
        void hasStock_checksIfSufficientStock() {
            // assert
            assertThat(product.hasStock(5)).isTrue();
            assertThat(product.hasStock(10)).isTrue();
            assertThat(product.hasStock(11)).isFalse();
        }
    }
}
