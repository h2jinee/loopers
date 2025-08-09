package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProductStockEntity 테스트")
public class ProductStockEntityTest {

    private ProductStockEntity productStock;

    @BeforeEach
    void setUp() {
        productStock = ProductStockEntity.createFor(1L, 10);
    }

    @DisplayName("재고를 감소시킬 수 있다.")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10})
    void decreaseStock_successfully(int quantity) {
        // arrange
        int initialStock = productStock.getStock();

        // act
        productStock.decrease(quantity);

        // assert
        assertThat(productStock.getStock()).isEqualTo(initialStock - quantity);
    }

    @DisplayName("재고를 증가시킬 수 있다.")
    @Test
    void increaseStock_successfully() {
        // arrange
        int initialStock = productStock.getStock();

        // act
        productStock.increase(5);

        // assert
        assertThat(productStock.getStock()).isEqualTo(initialStock + 5);
    }

    @DisplayName("재고보다 많은 수량을 감소시키려 하면 예외가 발생한다.")
    @Test
    void throwsException_whenDecreasingMoreThanStock() {
        // assert
        assertThatThrownBy(() -> productStock.decrease(11))
            .isInstanceOf(com.loopers.support.error.CoreException.class)
            .hasMessageContaining("재고가 부족합니다");
    }

    @DisplayName("재고를 생성할 수 있다.")
    @Test
    void createProductStock_withInitialStock() {
        // act
        ProductStockEntity newStock = ProductStockEntity.createFor(2L, 20);

        // assert
        assertThat(newStock.getProductId()).isEqualTo(2L);
        assertThat(newStock.getStock()).isEqualTo(20);
    }
}
