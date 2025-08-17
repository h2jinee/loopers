package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.brand.BrandCacheDto;
import com.loopers.domain.brand.BrandDomainInfo;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.monitoring.DbCallChecker;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private static final String NULL_VALUE = "__NULL__";
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String BRAND_KEY_PREFIX = "brand:";
    private static final String PRODUCT_LIST_KEY_PREFIX = "products:list:";
    
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductStockService productStockService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final ProductCacheMapper cacheMapper;
    
    // DbCallChecker 인스턴스
    private DbCallChecker dbCallChecker;
    
    @PostConstruct
    public void init() {
        dbCallChecker = new DbCallChecker("ProductService");
        log.info("ProductService DbCallChecker initialized");
    }
    
    @PreDestroy
    public void destroy() {
        if (dbCallChecker != null) {
            dbCallChecker.logDbCall();
        }
    }
    
    /**
     * 5분마다 DB 호출 통계 로깅
     */
    @Scheduled(fixedDelay = 300000) // 5분
    public void logDbCallStats() {
        if (dbCallChecker != null) {
            dbCallChecker.logDbCall();
            dbCallChecker.reset(); // 통계 초기화
        }
    }

    /**
     * 상품 조회(NULL 값 캐싱으로 Cache Penetration 방지, Cache-Aside 패턴)
     */
    public ProductDomainInfo getProduct(ProductCommand.GetOne command) {
        String key = PRODUCT_KEY_PREFIX + command.productId();
        
        try {
            // 1. 캐시에서 먼저 조회
            String cached = redisTemplate.opsForValue().get(key);
            
            // 2. NULL_VALUE가 캐싱되어 있으면 존재하지 않는 상품
            if (NULL_VALUE.equals(cached)) {
                meterRegistry.counter("cache.hit", "type", "null").increment();
                throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. ID: " + command.productId());
            }
            
            // 3. 캐시에 값이 있으면 바로 반환 (DB 조회하지 않음)
            if (cached != null) {
                try {
                    meterRegistry.counter("cache.hit", "type", "product").increment();
                    ProductCacheDto cacheDto = objectMapper.readValue(cached, ProductCacheDto.class);
                    return ProductDomainInfo.from(cacheDto);
                } catch (JsonProcessingException e) {
                    // 역직렬화 실패 시 잘못된 캐시 삭제
                    log.warn("잘못된 캐시 데이터 감지, 삭제 후 재조회: {}", command.productId());
                    redisTemplate.delete(key);
                }
            }
            
            meterRegistry.counter("cache.miss", "type", "product").increment();
            
            // 4. 캐시 미스 - DB에서 조회
            dbCallChecker.incrementDbSelectCount();
            ProductEntity product = productRepository.findById(command.productId()).orElse(null);
            meterRegistry.counter("db.query", "table", "product").increment();
            
            // 5. 조회 결과 캐싱 (NULL도 캐싱)
            if (product == null) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, 5, TimeUnit.MINUTES);
                throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. ID: " + command.productId());
            }
            
            // Entity를 CacheDto로 변환 후 캐싱
            ProductCacheDto cacheDto = ProductCacheDto.from(product);
            log.debug("CacheDto created: id={}, price={}", cacheDto.id(), cacheDto.price());
            String serialized = objectMapper.writeValueAsString(cacheDto);
            log.debug("Serialized cache: {}", serialized);
            redisTemplate.opsForValue().set(key, serialized, 5, TimeUnit.MINUTES);
            
            return ProductDomainInfo.from(product);
            
        } catch (JsonProcessingException e) {
            log.error("상품 직렬화/역직렬화 실패: {}", command.productId(), e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "데이터 처리 오류");
        }
    }
    
    
    /**
     * 상품 목록 조회 - 페이징 (비정규화된 like_count 사용)
     */
    public Page<ProductEntity> getProductList(ProductCommand.GetList command) {
        dbCallChecker.incrementDbSelectCount(); // DB 호출 카운트
        Pageable pageable = createPageable(command);
        
        // 브랜드별 조회
        if (command.brandId() != null) {
            return productRepository.findByBrandIdWithLikeCount(command.brandId(), pageable);
        }
        return productRepository.findAllWithLikeCount(pageable);
    }
    
    /**
     * 상품과 브랜드 정보 함께 조회
     */
    public ProductWithBrand getProductWithBrand(ProductCommand.GetOne command) {
        // 상품 정보 조회 (캐시)
        ProductDomainInfo product = getProduct(command);
        
        // 브랜드 정보 조회 (캐시)
        BrandDomainInfo brand = findBrandById(product.brandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        
        return new ProductWithBrand(product, brand);
    }
    
    /**
     * 브랜드 조회(NULL 값 캐싱)
     */
    public Optional<BrandDomainInfo> findBrandById(Long brandId) {
        String key = BRAND_KEY_PREFIX + brandId;
        
        try {
            // 1. 캐시에서 먼저 조회
            String cached = redisTemplate.opsForValue().get(key);
            
            // 2. NULL_VALUE가 캐싱되어 있으면 empty 반환
            if (NULL_VALUE.equals(cached)) {
                meterRegistry.counter("cache.hit", "type", "null").increment();
                return Optional.empty();
            }
            
            // 3. 캐시에 값이 있으면 바로 반환 (DB 조회하지 않음)
            if (cached != null) {
                meterRegistry.counter("cache.hit", "type", "brand").increment();
                BrandCacheDto cacheDto = objectMapper.readValue(cached, BrandCacheDto.class);
                return Optional.of(BrandDomainInfo.from(cacheDto));
            }
            
            meterRegistry.counter("cache.miss", "type", "brand").increment();
            
            // 4. 캐시 미스 - DB에서 조회
            dbCallChecker.incrementDbSelectCount();
            Optional<BrandEntity> brand = brandRepository.findById(brandId);
            meterRegistry.counter("db.query", "table", "brand").increment();
            
            // 5. 조회 결과 캐싱 (NULL도 캐싱)
            if (brand.isEmpty()) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, 5, TimeUnit.MINUTES);
                return Optional.empty();
            }
            
            // Entity를 CacheDto로 변환 후 캐싱
            BrandCacheDto cacheDto = BrandCacheDto.from(brand.get());
            String serialized = objectMapper.writeValueAsString(cacheDto);
            redisTemplate.opsForValue().set(key, serialized, 5, TimeUnit.MINUTES);
            
            return Optional.of(BrandDomainInfo.from(brand.get()));
            
        } catch (JsonProcessingException e) {
            log.error("브랜드 직렬화/역직렬화 실패: {}", brandId, e);
            return Optional.empty();
        }
    }
    
    
    /**
     * 상품 목록 + 브랜드 정보 조회 (재고 정보 캐싱 제외)
     */
    public Page<ProductWithBrandDto> getProductListWithBrand(ProductCommand.GetList command) {
        // 캐시 키 생성: brandId:sort:page:size
        String cacheKey = PRODUCT_LIST_KEY_PREFIX + 
            (command.brandId() != null ? command.brandId() : "all") + ":" +
            command.sort().name().toLowerCase() + ":" +
            command.page() + ":" +
            command.size();
        
        try {
            // 1. 캐시 확인
            log.debug("캐시 키 조회 시도: {}", cacheKey);
            String cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                try {
                    // 캐시 히트
                    log.info("캐시 히트, 키: {}", cacheKey);
                    meterRegistry.counter("cache.hit", "type", "product_list").increment();
                    ProductListCacheDto cacheDto = objectMapper.readValue(cached, ProductListCacheDto.class);
                    
                    // 캐시된 데이터를 Page<ProductWithBrandDto>로 변환
                    Pageable pageable = createPageable(command);
                    return cacheMapper.convertToPage(cacheDto, pageable);
                } catch (JsonProcessingException e) {
                    // 역직렬화 실패 시 잘못된 캐시 삭제
                    log.warn("잘못된 캐시 데이터 감지, 삭제 후 재조회: {}", cacheKey);
                    redisTemplate.delete(cacheKey);
                    // 캐시 미스로 처리하여 아래 로직 계속 진행
                }
            }
            
            // 2. 캐시 미스 - DB 조회
            log.info("캐시 미스, 키: {}, DB 조회 시작", cacheKey);
            meterRegistry.counter("cache.miss", "type", "product_list").increment();
            log.debug("DB에서 상품 목록 조회 - brandId: {}, sort: {}, page: {}", 
                      command.brandId(), command.sort(), command.page());
            
            dbCallChecker.incrementDbSelectCount();
            Pageable pageable = createPageable(command);
            
            Page<ProductWithBrandDto> result = command.brandId() != null
                ? productRepository.findProductsWithBrandByBrandId(command.brandId(), pageable)
                : productRepository.findAllProductsWithBrand(pageable);
            
            // 3. 결과를 캐싱 (재고 정보 제외)
            if (!result.isEmpty()) {
                ProductListCacheDto cacheDto = ProductListCacheDto.from(result);
                String serialized = objectMapper.writeValueAsString(cacheDto);
                redisTemplate.opsForValue().set(cacheKey, serialized, 3, TimeUnit.MINUTES); // 3분 TTL
            }
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("상품 목록 직렬화/역직렬화 실패", e);
            // 캐시 실패 시 DB 직접 조회
            dbCallChecker.incrementDbSelectCount();
            Pageable pageable = createPageable(command);
            
            return command.brandId() != null
                ? productRepository.findProductsWithBrandByBrandId(command.brandId(), pageable)
                : productRepository.findAllProductsWithBrand(pageable);
        }
    }
    
    
    /**
     * 상품 목록 + 재고 정보 조회
     */
    public Page<ProductWithBrandAndStock> getProductListWithBrandAndStock(ProductCommand.GetList command) {
        // 상품과 브랜드 정보는 캐시에서 조회
        Page<ProductWithBrandDto> productsWithBrand = getProductListWithBrand(command);
        
        if (productsWithBrand.isEmpty()) {
            return Page.empty(createPageable(command));
        }
        
        // 재고 정보 DB에서 조회 (캐싱 X)
        List<Long> productIds = productsWithBrand.getContent().stream()
            .map(ProductWithBrandDto::productId)
            .collect(Collectors.toList());
        
        dbCallChecker.incrementDbSelectCount(); // DB 호출 카운트
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
    public void decreaseStock(ProductCommand.DecreaseStock command) {
        if (!productStockService.isAvailable(command.productId())) {
            throw new CoreException(ErrorType.CONFLICT, "구매할 수 없는 상품입니다.");
        }
        
        productStockService.decreaseStock(command.productId(), command.quantity());
        
        // 캐시 무효화
        String key = PRODUCT_KEY_PREFIX + command.productId();
        redisTemplate.delete(key);
        
        log.debug("재고 감소 및 캐시 무효화 - productId: {}, quantity: {}", 
                  command.productId(), command.quantity());
    }
    
    /**
     * 재고 증가 - 캐시 무효화 포함
     */
    @Transactional
    public void increaseStock(ProductCommand.IncreaseStock command) {
        productStockService.increaseStock(command.productId(), command.quantity());
        
        // 캐시 무효화
        String key = PRODUCT_KEY_PREFIX + command.productId();
        redisTemplate.delete(key);
        
        log.debug("재고 증가 및 캐시 무효화 - productId: {}, quantity: {}", 
                  command.productId(), command.quantity());
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
        ProductDomainInfo product,
        BrandDomainInfo brand
    ) {}
    
    public record ProductWithBrandAndStock(
        ProductWithBrandDto productWithBrand,
        ProductStockInfo stockInfo
    ) {}
}
