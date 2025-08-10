package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;

public class ProductCriteria {
    
    public record GetDetail(
        Long productId
    ) {
        public ProductCommand.GetOne toCommand() {
            return new ProductCommand.GetOne(productId);
        }
    }
    
    public record GetList(
        Long brandId,
        String sort,
        Integer page,
        Integer size
    ) {
        public ProductCommand.GetList toCommand() {
            return ProductCommand.GetList.of(brandId, sort, page, size);
        }
    }
}