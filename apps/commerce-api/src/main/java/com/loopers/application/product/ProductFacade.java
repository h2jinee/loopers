package com.loopers.application.product;

import com.loopers.application.stock.StockFacade;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    
    private final ProductService productService;
    private final StockFacade stockFacade;
    
    @Transactional(readOnly = true)
    public ProductResult.Detail getProductDetail(ProductCriteria.GetDetail criteria) {
        ProductCommand.GetOne command = criteria.toCommand();
        ProductService.ProductWithBrand productWithBrand = productService.getProductWithBrand(command);
        
        // 재고 정보를 StockFacade에서 조회
        ProductStockInfo stockInfo = new ProductStockInfo(
            productWithBrand.product().id(),
            stockFacade.getStock(productWithBrand.product().id()),
            stockFacade.isAvailable(productWithBrand.product().id())
        );
        
        ProductInfo.Detail domainInfo = ProductInfo.Detail.from(
            productWithBrand.product(),
            productWithBrand.brand(),
            stockInfo
        );
        return ProductResult.Detail.from(domainInfo);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResult.Summary> getProductList(ProductCriteria.GetList criteria) {
        ProductCommand.GetList command = criteria.toCommand();
        Page<ProductService.ProductWithBrandAndStock> productsWithBrandAndStock = 
            productService.getProductListWithBrandAndStock(command);
        
        return productsWithBrandAndStock.map(item -> {
            ProductInfo.Summary domainInfo = ProductInfo.Summary.from(item);
            return ProductResult.Summary.from(domainInfo);
        });
    }
}
