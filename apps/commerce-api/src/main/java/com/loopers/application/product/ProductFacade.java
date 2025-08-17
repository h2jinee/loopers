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
        ProductCommand.GetOne command = criteria.toCommand();
        ProductService.ProductWithBrand productWithBrand = productService.getProductWithBrand(command);
        
        ProductInfo.Detail domainInfo = ProductInfo.Detail.from(productWithBrand, productStockService);
        return ProductResult.Detail.from(domainInfo);
    }
    
    public Page<ProductResult.Summary> getProductList(ProductCriteria.GetList criteria) {
        ProductCommand.GetList command = criteria.toCommand();
        Page<ProductService.ProductWithBrandAndStock> productsWithBrandAndStock = productService.getProductListWithBrandAndStock(command);
        
        return productsWithBrandAndStock.map(item -> {
            ProductInfo.Summary domainInfo = ProductInfo.Summary.from(item);
            return ProductResult.Summary.from(domainInfo);
        });
    }
}
