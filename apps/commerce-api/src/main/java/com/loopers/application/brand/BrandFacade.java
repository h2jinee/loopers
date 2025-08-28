package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandFacade {
    
    private final BrandService brandService;
    private final ProductService productService;
    
    public BrandResult.Detail getBrandDetail(BrandCriteria.GetDetail criteria) {
        // 1. 브랜드 기본 정보 조회
        BrandCommand.GetOne getCommand = criteria.toCommand();
        BrandInfo brandInfo = brandService.getBrand(getCommand);
        
        // 2. 해당 브랜드의 상품 개수 조회
        ProductCommand.GetList command = ProductCommand.GetList.of(criteria.brandId(), null, 0, 1);
        Page<ProductInfo> products = productService.getProducts(command);
        Integer productCount = (int) products.getTotalElements();
        
        // 3. 결과 생성
        return BrandResult.Detail.from(brandInfo, productCount);
    }
    
    public Page<BrandResult.Summary> getBrandList(BrandCriteria.GetList criteria) {
        // 1. 브랜드 목록 조회
        BrandCommand.GetList command = criteria.toCommand();
        Page<BrandInfo> brands = brandService.getBrandList(command);
        
        // 2. 직접 변환
        return brands.map(BrandResult.Summary::from);
    }
}
