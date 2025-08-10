package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductCommand;
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
        BrandEntity brand = brandService.getBrand(getCommand);
        
        // 2. 해당 브랜드의 상품 개수 조회
        ProductCommand.GetList command = ProductCommand.GetList.of(criteria.brandId(), null, 0, 1);
        Page<com.loopers.domain.product.ProductEntity> products = productService.getProductList(command);
        Integer productCount = (int) products.getTotalElements();
        
        // 3. 브랜드 상세 정보와 상품 개수를 결합하여 응답 생성
        BrandInfo.Detail domainInfo = BrandInfo.Detail.from(brand, productCount);
        return BrandResult.Detail.from(domainInfo);
    }
    
    public Page<BrandResult.Summary> getBrandList(BrandCriteria.GetList criteria) {
        // 1. 브랜드 목록 조회
        BrandCommand.GetList command = criteria.toCommand();
        Page<BrandEntity> brands = brandService.getBrandList(command);
        
        // 2. 각 브랜드를 응답 형태로 변환
        return brands.map(brand -> {
            BrandInfo.Summary domainInfo = BrandInfo.Summary.from(brand);
            return BrandResult.Summary.from(domainInfo);
        });
    }
}
