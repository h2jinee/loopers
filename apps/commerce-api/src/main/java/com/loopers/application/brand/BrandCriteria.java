package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCommand;

public class BrandCriteria {
    
    public record GetDetail(
        Long brandId
    ) {
        public BrandCommand.GetOne toCommand() {
            return new BrandCommand.GetOne(brandId);
        }
    }
    
    public record GetList(
        Integer page,
        Integer size
    ) {
        public BrandCommand.GetList toCommand() {
            return BrandCommand.GetList.of(page, size);
        }
    }
}