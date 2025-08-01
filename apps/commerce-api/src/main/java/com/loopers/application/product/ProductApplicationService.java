package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductApplicationService {
    
    private final ProductDomainService productDomainService;
    
    public ProductInfo.Detail getProductDetail(Long productId) {
        ProductCommand.GetOne command = new ProductCommand.GetOne(productId);
        ProductDomainService.ProductWithBrand productWithBrand = productDomainService.getProductWithBrand(command);
        
        return ProductInfo.Detail.from(productWithBrand.product(), productWithBrand.getBrandName());
    }
    
    public Page<ProductInfo.Summary> getProductList(Long brandId, String sort, Integer page, Integer size) {
        ProductCommand.GetList command = ProductCommand.GetList.of(brandId, sort, page, size);
        Page<ProductDomainService.ProductWithBrand> productsWithBrand = productDomainService.getProductListWithBrand(command);
        
        return productsWithBrand.map(pwb -> ProductInfo.Summary.from(pwb.product(), pwb.getBrandName()));
    }
}
