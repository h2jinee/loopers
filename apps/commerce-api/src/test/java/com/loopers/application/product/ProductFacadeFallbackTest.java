package com.loopers.application.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("ProductFacade Fallback 테스트")
public class ProductFacadeFallbackTest {

    @Autowired
    private ProductFacade productFacade;
    
    @Test
    @DisplayName("상품 상세 조회 Fallback 메서드가 정상 작동한다")
    void getProductDetailFallback_WorksProperly() {
        // given
        ProductCriteria.GetDetail criteria = new ProductCriteria.GetDetail(1L);
        RuntimeException exception = new RuntimeException("DB 연결 실패");
        
        // when - Fallback 메서드 직접 호출
        ProductResult.Detail result = productFacade.getProductDetailFallback(criteria, exception);
        
        // then
        assertThat(result).isNotNull();
        // Fallback에서 반환하는 기본값들 확인
    }
    
    @Test
    @DisplayName("상품 목록 조회 Fallback 메서드가 정상 작동한다")
    void getProductListFallback_WorksProperly() {
        // given
        ProductCriteria.GetList criteria = new ProductCriteria.GetList(
            null, "createdAt", 0, 10
        );
        RuntimeException exception = new RuntimeException("DB 연결 실패");
        
        // when - Fallback 메서드 직접 호출
        Page<ProductResult.Summary> result = productFacade.getProductListFallback(exception);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}