package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductInfo getProduct(ProductCommand.GetOne command) {
        Product product = productRepository.findById(command.productId())
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, 
                "상품을 찾을 수 없습니다. ID: " + command.productId()
            ));
        
        return ProductInfo.from(product);
    }
    
    public Page<ProductInfo> getProducts(ProductCommand.GetList command) {
        Pageable pageable = createPageable(command);
        
        Page<Product> products = command.brandId() != null
            ? productRepository.findByBrandIdWithLikeCount(command.brandId(), pageable)
            : productRepository.findAllWithLikeCount(pageable);
        
        return products.map(ProductInfo::from);
    }

    public Map<Long, ProductInfo> getProductsByIds(List<Long> productIds) {
        return productRepository.findByIdIn(productIds).stream()
            .collect(Collectors.toMap(
                Product::getId,
                ProductInfo::from
            ));
    }
    
    private Pageable createPageable(ProductCommand.GetList command) {
        Sort sort = switch (command.sort()) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case LIKES_DESC -> Sort.by(Sort.Direction.DESC, "likeCount");
        };
        
        return PageRequest.of(command.page(), command.size(), sort);
    }
}
