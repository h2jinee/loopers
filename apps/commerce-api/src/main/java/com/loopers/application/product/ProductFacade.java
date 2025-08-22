package com.loopers.application.product;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.*;
import com.loopers.domain.stock.StockInfo;
import com.loopers.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacade {
    
    private final ProductService productService;
    private final BrandService brandService;
    private final StockService stockService;
    
    public ProductResult.Detail getProductDetail(ProductCriteria.GetDetail criteria) {
        // 1. 상품 정보 조회
        ProductInfo productInfo = productService.getProduct(
            new ProductCommand.GetOne(criteria.productId())
        );
        
        // 2. 브랜드 정보 조회
        BrandInfo brandInfo = brandService.getBrand(
            new BrandCommand.GetOne(productInfo.brandId())
        );
        
        // 3. 재고 정보 조회
        StockInfo stockInfo = stockService.getStockInfo(criteria.productId());
        
        // 4. 결과 조합
        return ProductResult.Detail.from(productInfo, brandInfo, stockInfo);
    }
    
    public Page<ProductResult.Summary> getProductList(ProductCriteria.GetList criteria) {
        // 1. 상품 목록 조회
        Page<ProductInfo> products = productService.getProducts(criteria.toCommand());
        
        // 2. 필요한 ID들 추출
        List<Long> productIds = products.map(ProductInfo::productId).toList();
        List<Long> brandIds = products.stream()
            .map(ProductInfo::brandId)
            .distinct()
            .toList();
        
        // 3. 벌크 조회 (N+1 방지)
        Map<Long, BrandInfo> brandMap = brandService.getBrandsByIds(brandIds);
        Map<Long, StockInfo> stockMap = stockService.getStockInfosByProductIds(productIds);
        
        // 4. 결과 조합
        return products.map(product -> {
            BrandInfo brand = brandMap.get(product.brandId());
            StockInfo stock = stockMap.get(product.productId());

            return ProductResult.Summary.from(product, brand, stock);
        });
    }
}
