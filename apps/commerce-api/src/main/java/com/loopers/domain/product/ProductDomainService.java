package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDomainService {
    
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductStockService productStockService;

    public ProductEntity getProduct(ProductCommand.GetOne command) {
        return getProductById(command.productId());
    }
    
    private ProductEntity getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
    
    public Page<ProductEntity> getProductList(ProductCommand.GetList command) {
        Pageable pageable = createPageable(command);
        
        if (command.brandId() != null) {
            return productRepository.findByBrandIdWithLikeCount(command.brandId(), pageable);
        }
        
        return productRepository.findAllWithLikeCount(pageable);
    }
    
    public void decreaseStock(ProductCommand.DecreaseStock command) {
        if (!productStockService.isAvailable(command.productId())) {
            throw new CoreException(ErrorType.CONFLICT, "구매할 수 없는 상품입니다.");
        }
        
        productStockService.decreaseStock(command.productId(), command.quantity());
    }
    
    public void increaseStock(ProductCommand.IncreaseStock command) {
        productStockService.increaseStock(command.productId(), command.quantity());
    }

    private Pageable createPageable(ProductCommand.GetList command) {
        Sort sort = switch (command.sort()) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case LIKES_DESC -> Sort.by(Sort.Direction.DESC, "likeCount");
        };
        
        return PageRequest.of(command.page(), command.size(), sort);
    }
    
    public void validateProductExists(ProductCommand.ValidateExists command) {
        if (!productRepository.existsById(command.productId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
    }
    
    public ProductWithBrand getProductWithBrand(ProductCommand.GetOne command) {
        ProductEntity product = getProductById(command.productId());
        BrandEntity brand = brandRepository.findById(product.getBrandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        
        return new ProductWithBrand(product, brand);
    }
    
    public Page<ProductWithBrand> getProductListWithBrand(ProductCommand.GetList command) {
        Page<ProductEntity> products = getProductList(command);
        
        return products.map(product -> {
            BrandEntity brand = brandRepository.findById(product.getBrandId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
            return new ProductWithBrand(product, brand);
        });
    }
    
    public record ProductWithBrand(
        ProductEntity product,
        BrandEntity brand
    ) {
        public String getBrandName() {
            return brand.getNameKo();
        }
    }
}
