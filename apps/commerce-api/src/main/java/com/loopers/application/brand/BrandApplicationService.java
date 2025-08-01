package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandDomainService;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandApplicationService {
    
    private final BrandDomainService brandDomainService;
    private final ProductDomainService productDomainService;
    
    public BrandInfo.Detail getBrandDetail(Long brandId) {
        BrandCommand.GetOne getCommand = new BrandCommand.GetOne(brandId);
        BrandEntity brand = brandDomainService.getBrand(getCommand);
        
        // 해당 브랜드의 상품 개수 조회
        ProductCommand.GetList command = ProductCommand.GetList.of(brandId, null, 0, 1);
        Page<com.loopers.domain.product.ProductEntity> products = productDomainService.getProductList(command);
        Integer productCount = (int) products.getTotalElements();
        
        return BrandInfo.Detail.from(brand, productCount);
    }
    
    public Page<BrandInfo.Summary> getBrandList(Integer page, Integer size) {
        BrandCommand.GetList command = BrandCommand.GetList.of(page, size);
        Page<BrandEntity> brands = brandDomainService.getBrandList(command);
        
        return brands.map(BrandInfo.Summary::from);
    }
}
