package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductApplicationService;
import com.loopers.application.product.ProductInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {
    
    private final ProductApplicationService productApplicationService;
    
    @GetMapping
    @Override
    public ApiResponse<Page<ProductDto.V1.GetList.Response>> getProductList(
        @RequestParam(required = false) Long brandId,
        @RequestParam(required = false, defaultValue = "latest") String sort,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        Page<ProductInfo.Summary> products = productApplicationService.getProductList(brandId, sort, page, size);
        Page<ProductDto.V1.GetList.Response> response = products.map(ProductDto.V1.GetList.Response::from);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductDto.V1.GetDetail.Response> getProductDetail(
        @PathVariable Long productId
    ) {
        ProductInfo.Detail detail = productApplicationService.getProductDetail(productId);
        ProductDto.V1.GetDetail.Response response = ProductDto.V1.GetDetail.Response.from(detail);
        return ApiResponse.success(response);
    }
}
