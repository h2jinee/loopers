package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandCriteria;
import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {
    
    private final BrandFacade brandFacade;
    
    @GetMapping
    @Override
    public ApiResponse<Page<BrandDto.V1.GetList.Response>> getBrandList(
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        BrandCriteria.GetList criteria = new BrandCriteria.GetList(page, size);
        Page<BrandResult.Summary> brands = brandFacade.getBrandList(criteria);
        Page<BrandDto.V1.GetList.Response> response = brands.map(BrandDto.V1.GetList.Response::from);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandDto.V1.GetDetail.Response> getBrandDetail(
        @PathVariable Long brandId
    ) {
        BrandCriteria.GetDetail criteria = new BrandCriteria.GetDetail(brandId);
        BrandResult.Detail detail = brandFacade.getBrandDetail(criteria);
        BrandDto.V1.GetDetail.Response response = BrandDto.V1.GetDetail.Response.from(detail);
        return ApiResponse.success(response);
    }
}
