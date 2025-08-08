package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    
    private final ProductService productService;
    private final ProductStockService productStockService;
    
    public ProductResult.Detail getProductDetail(ProductCriteria.GetDetail criteria) {
        // 1. 상품 정보와 브랜드 정보를 함께 조회
        ProductCommand.GetOne command = criteria.toCommand();
        ProductService.ProductWithBrand productWithBrand = productService.getProductWithBrand(command);
        
        // 2. 상품 상세 정보에 재고 정보를 포함하여 응답 생성
        ProductInfo.Detail domainInfo = ProductInfo.Detail.from(productWithBrand.product(), productWithBrand.getBrandName(), productStockService);
        return ProductResult.Detail.from(domainInfo);
    }
    
    public Page<ProductResult.Summary> getProductList(ProductCriteria.GetList criteria) {
        // 1. 상품 목록과 브랜드 정보를 함께 조회
        ProductCommand.GetList command = criteria.toCommand();
        Page<ProductService.ProductWithBrand> productsWithBrand = productService.getProductListWithBrand(command);
        
        // 2. 각 상품을 재고 정보와 함께 응답 형태로 변환
        return productsWithBrand.map(pwb -> {
            ProductInfo.Summary domainInfo = ProductInfo.Summary.from(pwb.product(), pwb.getBrandName(), productStockService);
            return ProductResult.Summary.from(domainInfo);
        });
    }
}
