package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
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
public class ProductService {
    
    private final ProductJpaRepository productJpaRepository;
    private final BrandJpaRepository brandJpaRepository;
    private final ProductStockService productStockService;

    public ProductEntity getProduct(ProductCommand.GetOne command) {
        return getProductById(command.productId());
    }
    
    private ProductEntity getProductById(Long productId) {
        return productJpaRepository.findById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
    
    public Page<ProductEntity> getProductList(ProductCommand.GetList command) {
        Pageable pageable = createPageable(command);
        
        if (command.brandId() != null) {
            return productJpaRepository.findByBrandIdWithLikeCount(command.brandId(), pageable);
        }
        
        return productJpaRepository.findAllWithLikeCount(pageable);
    }
    
    public void decreaseStock(ProductCommand.DecreaseStock command) {
        // 1. 재고 구매 가능 여부 확인
        if (!productStockService.isAvailable(command.productId())) {
            throw new CoreException(ErrorType.CONFLICT, "구매할 수 없는 상품입니다.");
        }
        
        // 2. 재고 차감 처리
        productStockService.decreaseStock(command.productId(), command.quantity());
    }
    
    public void increaseStock(ProductCommand.IncreaseStock command) {
        // 재고 증가 처리
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

    public ProductWithBrand getProductWithBrand(ProductCommand.GetOne command) {
        // 1. 상품 정보 조회
        ProductEntity product = getProductById(command.productId());
        
        // 2. 해당 상품의 브랜드 정보 조회
        BrandEntity brand = brandJpaRepository.findById(product.getBrandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        
        // 3. 상품과 브랜드 정보 결합
        return new ProductWithBrand(product, brand);
    }
    
    public Page<ProductWithBrand> getProductListWithBrand(ProductCommand.GetList command) {
        // 1. 상품 목록 조회
        Page<ProductEntity> products = getProductList(command);
        
        // 2. 각 상품에 대해 브랜드 정보를 결합
        return products.map(product -> {
            BrandEntity brand = brandJpaRepository.findById(product.getBrandId())
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
