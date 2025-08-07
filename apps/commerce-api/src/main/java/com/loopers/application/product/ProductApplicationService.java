package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductDomainService;
import com.loopers.domain.product.ProductStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    
    private final ProductDomainService productDomainService;
    private final ProductStockService productStockService;
    
    public ProductInfo.Detail getProductDetail(Long productId) {
        ProductCommand.GetOne command = new ProductCommand.GetOne(productId);
        ProductDomainService.ProductWithBrand productWithBrand = productDomainService.getProductWithBrand(command);
        
        return ProductInfo.Detail.from(productWithBrand.product(), productWithBrand.getBrandName(), productStockService);
    }
    
    public Page<ProductInfo.Summary> getProductList(Long brandId, String sort, Integer page, Integer size) {
        ProductCommand.GetList command = ProductCommand.GetList.of(brandId, sort, page, size);
        Page<ProductDomainService.ProductWithBrand> productsWithBrand = productDomainService.getProductListWithBrand(command);
        
        return productsWithBrand.map(pwb -> ProductInfo.Summary.from(pwb.product(), pwb.getBrandName(), productStockService));
    }
}
