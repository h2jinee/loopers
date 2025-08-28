package com.loopers.domain.stock;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Stock 엔티티 테스트")
public class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock(1L, 10);
    }

    @DisplayName("재고를 감소시킬 수 있다.")
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10})
    void decreaseStock_successfully(int quantity) {
        // arrange
        int initialStock = stock.getQuantity();

        // act
        stock.decrease(quantity);

        // assert
        assertThat(stock.getQuantity()).isEqualTo(initialStock - quantity);
    }

    @DisplayName("재고보다 많은 수량을 감소시키려 하면 예외가 발생한다.")
    @Test
    void throwsException_whenDecreasingMoreThanStock() {
        // assert
        assertThatThrownBy(() -> stock.decrease(11))
            .isInstanceOf(com.loopers.support.error.CoreException.class)
            .hasMessageContaining("재고가 부족합니다");
    }

    @DisplayName("재고를 생성할 수 있다.")
    @Test
    void createStock_withInitialQuantity() {
        // act
        Stock newStock = new Stock(2L, 20);

        // assert
        assertThat(newStock.getProductId()).isEqualTo(2L);
        assertThat(newStock.getQuantity()).isEqualTo(20);
    }

    @DisplayName("재고를 조정할 수 있다.")
    @Test
    void adjustStock_successfully() {
        // act
        stock.adjust(30);

        // assert
        assertThat(stock.getQuantity()).isEqualTo(30);
    }

    @DisplayName("재고 사용 가능 여부를 확인할 수 있다.")
    @Test
    void checkAvailability() {
        // assert
        assertThat(stock.isAvailable(5)).isTrue();
        assertThat(stock.isAvailable(10)).isTrue();
        assertThat(stock.isAvailable(11)).isFalse();
    }

    @DisplayName("재고 소진 여부를 확인할 수 있다.")
    @Test
    void checkOutOfStock() {
        // arrange
        Stock emptyStock = new Stock(3L, 0);

        // assert
        assertThat(emptyStock.isOutOfStock()).isTrue();
        assertThat(stock.isOutOfStock()).isFalse();
    }

    @DisplayName("음수 재고로 생성하려 하면 예외가 발생한다.")
    @Test
    void throwsException_whenCreatingWithNegativeQuantity() {
        // assert
        assertThatThrownBy(() -> new Stock(1L, -1))
            .isInstanceOf(com.loopers.support.error.CoreException.class)
            .hasMessageContaining("재고 수량은 0 이상이어야 합니다");
    }

    @DisplayName("null productId로 생성하려 하면 예외가 발생한다.")
    @Test
    void throwsException_whenCreatingWithNullProductId() {
        // assert
        assertThatThrownBy(() -> new Stock(null, 10))
            .isInstanceOf(com.loopers.support.error.CoreException.class)
            .hasMessageContaining("상품 ID는 필수입니다");
    }
}
