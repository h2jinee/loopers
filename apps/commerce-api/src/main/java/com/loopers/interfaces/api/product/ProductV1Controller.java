package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {
    
    private final ProductFacade productFacade;
    
    @GetMapping
    @Override
    public ApiResponse<Page<ProductDto.V1.GetList.Response>> getProductList(
        @RequestParam(required = false) Long brandId,
        @RequestParam(required = false, defaultValue = "latest") String sort,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        ProductCriteria.GetList criteria = new ProductCriteria.GetList(brandId, sort, page, size);
        Page<ProductResult.Summary> products = productFacade.getProductList(criteria);
        Page<ProductDto.V1.GetList.Response> response = products.map(ProductDto.V1.GetList.Response::from);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductDto.V1.GetDetail.Response> getProductDetail(
        @PathVariable Long productId
    ) {
        ProductCriteria.GetDetail criteria = new ProductCriteria.GetDetail(productId);
        ProductResult.Detail detail = productFacade.getProductDetail(criteria);
        ProductDto.V1.GetDetail.Response response = ProductDto.V1.GetDetail.Response.from(detail);
        return ApiResponse.success(response);
    }
}
