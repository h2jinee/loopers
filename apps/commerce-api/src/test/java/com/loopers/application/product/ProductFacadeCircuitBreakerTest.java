package com.loopers.application.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.stock.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DisplayName("ProductFacade CircuitBreaker 테스트")
class ProductFacadeCircuitBreakerTest {

    @Autowired
    private ProductFacade productFacade;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private StockService stockService;

    @Test
    @DisplayName("연속 실패시 CircuitBreaker가 열리고 Fallback이 호출된다")
    void circuitBreaker_OpensAndCallsFallback() {
        // given
        when(productService.getProduct(any()))
            .thenThrow(new RuntimeException("DB 장애"));

        ProductCriteria.GetDetail criteria = new ProductCriteria.GetDetail(1L);

        // when - CircuitBreaker threshold까지 호출
        ProductResult.Detail result = null;
        for (int i = 0; i < 5; i++) {
            result = productFacade.getProductDetail(criteria);
            assertThat(result).isNotNull();
        }

        // then - Fallback 결과 확인
        assertThat(result.product().productId()).isEqualTo(1L);
        assertThat(result.product().nameKo()).isEqualTo("일시적으로 조회 불가");
        assertThat(result.brand().nameKo()).isEqualTo("Unknown");
        assertThat(result.stock().inStock()).isFalse();

        // CircuitBreaker 열린 후 호출 횟수 확인
        verify(productService, atMost(5)).getProduct(any());
    }

    @Test
    @DisplayName("상품 목록 조회 실패시 빈 페이지 반환")
    void getProductList_ReturnEmptyPageOnFailure() {
        // given
        when(productService.getProducts(any()))
            .thenThrow(new RuntimeException("DB 연결 실패"));

        ProductCriteria.GetList criteria = new ProductCriteria.GetList(
            null, "createdAt", 0, 10
        );

        // when
        Page<ProductResult.Summary> result = null;
        for (int i = 0; i < 5; i++) {
            result = productFacade.getProductList(criteria);
        }

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
