package com.loopers.domain.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductStockService productStockService;

    /**
     * 상품 조회 - 외부 호출용
     */
    public ProductEntity getProduct(ProductCommand.GetOne command) {
        return findProductById(command.productId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
    
    /**
     * 상품 조회 - Optional로 캐싱 (Cache Penetration 방지)
     * Optional.empty()도 캐싱되므로 존재하지 않는 상품 ID에 대한 반복 조회 방지
     */
    @Cacheable(value = "products", key = "#productId")
    public Optional<ProductEntity> findProductById(Long productId) {
        log.debug("DB에서 상품 조회: {}", productId);
        return productRepository.findById(productId);
    }
    
    /**
     * 상품 목록 조회 - 페이징
     */
    public Page<ProductEntity> getProductList(ProductCommand.GetList command) {
        Pageable pageable = createPageable(command);
        
        if (command.brandId() != null) {
            return productRepository.findByBrandIdWithLikeCount(command.brandId(), pageable);
        }
        
        return productRepository.findAllWithLikeCount(pageable);
    }
    
    /**
     * 상품과 브랜드 정보 함께 조회
     */
    public ProductWithBrand getProductWithBrand(ProductCommand.GetOne command) {
        // 상품 정보는 캐시에서 조회
        ProductEntity product = getProduct(command);
        
        // 브랜드 정보도 캐싱
        BrandEntity brand = findBrandById(product.getBrandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        
        return new ProductWithBrand(product, brand);
    }
    
    /**
     * 브랜드 조회 - Optional로 캐싱
     */
    @Cacheable(value = "brands", key = "#brandId")
    public Optional<BrandEntity> findBrandById(Long brandId) {
        log.debug("DB에서 브랜드 조회: {}", brandId);
        return brandRepository.findById(brandId);
    }
    
    /**
     * 상품 목록과 브랜드, 재고 정보 함께 조회
     * 상품 목록과 브랜드 정보는 캐싱, 재고는 실시간 조회
     */
    @Cacheable(value = "productList", 
               key = "'list:' + #command.brandId() + ':' + #command.sort().name() + ':' + #command.page() + ':' + #command.size()")
    public Page<ProductWithBrandDto> getProductListWithBrand(ProductCommand.GetList command) {
        log.debug("DB에서 상품 목록 조회 - brandId: {}, sort: {}, page: {}", 
                  command.brandId(), command.sort(), command.page());
        
        Pageable pageable = createPageable(command);
        
        return command.brandId() != null
            ? productRepository.findProductsWithBrandByBrandId(command.brandId(), pageable)
            : productRepository.findAllProductsWithBrand(pageable);
    }
    
    /**
     * 상품 목록과 재고 정보 함께 조회
     * 캐시된 상품 목록에 실시간 재고 정보 결합
     */
    public Page<ProductWithBrandAndStock> getProductListWithBrandAndStock(ProductCommand.GetList command) {
        // 상품과 브랜드 정보는 캐시에서 조회
        Page<ProductWithBrandDto> productsWithBrand = getProductListWithBrand(command);
        
        if (productsWithBrand.isEmpty()) {
            return Page.empty(createPageable(command));
        }
        
        // 재고 정보는 실시간으로 DB에서 조회 (캐시하지 않음)
        List<Long> productIds = productsWithBrand.getContent().stream()
            .map(ProductWithBrandDto::productId)
            .collect(Collectors.toList());
        
        Map<Long, ProductStockInfo> stockInfoMap = productRepository.findProductStockInfoByIds(productIds)
            .stream()
            .collect(Collectors.toMap(
                ProductStockInfo::productId,
                info -> info,
                (existing, replacement) -> existing
            ));
        
        return productsWithBrand.map(dto -> new ProductWithBrandAndStock(
            dto,
            stockInfoMap.getOrDefault(dto.productId(), new ProductStockInfo(dto.productId(), 0, false))
        ));
    }
    
    /**
     * 재고 감소 - 캐시 무효화 포함
     */
    @Transactional
    @CacheEvict(value = "products", key = "#command.productId()")
    public void decreaseStock(ProductCommand.DecreaseStock command) {
        if (!productStockService.isAvailable(command.productId())) {
            throw new CoreException(ErrorType.CONFLICT, "구매할 수 없는 상품입니다.");
        }
        
        productStockService.decreaseStock(command.productId(), command.quantity());
        log.debug("재고 감소 및 캐시 무효화 - productId: {}, quantity: {}", 
                  command.productId(), command.quantity());
    }
    
    /**
     * 재고 증가 - 캐시 무효화 포함
     */
    @Transactional
    @CacheEvict(value = "products", key = "#command.productId()")
    public void increaseStock(ProductCommand.IncreaseStock command) {
        productStockService.increaseStock(command.productId(), command.quantity());
        log.debug("재고 증가 및 캐시 무효화 - productId: {}, quantity: {}", 
                  command.productId(), command.quantity());
    }
    
    /**
     * 좋아요 수 업데이트 - 상품 캐시와 목록 캐시 모두 무효화
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "#productId"),
        @CacheEvict(value = "productList", allEntries = true)
    })
    public void updateProductLikeCount(Long productId, Long likeCount) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setLikeCount(likeCount);
            productRepository.save(product);
            log.debug("좋아요 수 업데이트 및 캐시 무효화 - productId: {}, likeCount: {}", 
                      productId, likeCount);
        });
    }
    
    /**
     * 전체 상품 목록 조회 (캐싱 없이)
     */
    public Page<ProductWithBrand> getProductListWithBrandNonCached(ProductCommand.GetList command) {
        Page<ProductWithBrandAndStock> productsWithBrandAndStock = getProductListWithBrandAndStock(command);
        
        List<Long> productIds = productsWithBrandAndStock.getContent().stream()
            .map(item -> item.productWithBrand().productId())
            .collect(Collectors.toList());
        
        Map<Long, ProductEntity> productMap = productRepository.findAllByIdIn(productIds)
            .stream()
            .collect(Collectors.toMap(ProductEntity::getId, p -> p));
        
        List<Long> brandIds = productsWithBrandAndStock.getContent().stream()
            .map(item -> item.productWithBrand().brandId())
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, BrandEntity> brandMap = brandRepository.findAllById(brandIds)
            .stream()
            .collect(Collectors.toMap(BrandEntity::getId, b -> b));
        
        return productsWithBrandAndStock.map(item -> {
            ProductEntity product = productMap.get(item.productWithBrand().productId());
            BrandEntity brand = brandMap.get(item.productWithBrand().brandId());
            
            if (product != null) {
                product.setLikeCount(item.productWithBrand().likeCount());
            }
            
            return new ProductWithBrand(product, brand);
        });
    }
    
    private Pageable createPageable(ProductCommand.GetList command) {
        Sort sort = switch (command.sort()) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case LIKES_DESC -> Sort.by(Sort.Direction.DESC, "likeCount");
        };
        
        return PageRequest.of(command.page(), command.size(), sort);
    }
    
    public record ProductWithBrand(
        ProductEntity product,
        BrandEntity brand
    ) {
        public String getBrandName() {
            return brand != null ? brand.getNameKo() : null;
        }
    }
    
    public record ProductWithBrandAndStock(
        ProductWithBrandDto productWithBrand,
        ProductStockInfo stockInfo
    ) {}
}
