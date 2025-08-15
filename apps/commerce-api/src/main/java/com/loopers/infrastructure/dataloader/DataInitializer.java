package com.loopers.infrastructure.dataloader;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductStockEntity;
import com.loopers.domain.product.vo.ProductStatus;
import com.loopers.domain.user.UserEntity;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.product.ProductStockJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 굿즈샵 초기 데이터 생성기
 * 서버 시작 시 자동으로 데이터 생성
 * - 브랜드: 10개
 * - 상품: 10만개
 * - 회원: 100명
 * - 포인트: 회원별 자동 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BrandJpaRepository brandRepository;
    private final ProductJpaRepository productRepository;
    private final ProductStockJpaRepository stockRepository;
    private final UserJpaRepository userRepository;
    private final PointJpaRepository pointRepository;
    
    private final Random random = ThreadLocalRandom.current();
    private final Faker faker = new Faker(Locale.KOREA);
    
    // 설정값
    private static final int PRODUCT_COUNT = 100000;
    private static final int USER_COUNT = 100;

    // IP 브랜드 정보
    private static final List<IPBrand> IP_BRANDS = Arrays.asList(
        new IPBrand("디지몬", "Digimon", Arrays.asList("아구몬", "가부몬", "파피몬", "텐타몬", "팔몬", "쉬라몬")),
        new IPBrand("동물의 숲", "Animal Crossing", Arrays.asList("너굴", "토미", "티미", "여울", "부엉", "K.K.")),
        new IPBrand("나루토", "Naruto", Arrays.asList("나루토", "사스케", "사쿠라", "카카시", "가아라", "히나타")),
        new IPBrand("카드캡터 체리", "Cardcaptor Sakura", Arrays.asList("사쿠라", "케로", "토모요", "샤오란", "유키토")),
        new IPBrand("포켓몬스터", "Pokemon", Arrays.asList("피카츄", "이브이", "잠만보", "뮤", "리자몽", "꼬부기")),
        new IPBrand("원피스", "One Piece", Arrays.asList("루피", "조로", "나미", "우솝", "상디", "쵸파")),
        new IPBrand("스튜디오 지브리", "Studio Ghibli", Arrays.asList("토토로", "지지", "가오나시", "캘시퍼", "포뇨")),
        new IPBrand("산리오", "Sanrio", Arrays.asList("헬로키티", "마이멜로디", "쿠로미", "시나모롤", "폼폼푸린")),
        new IPBrand("미니언즈", "Minions", Arrays.asList("밥", "케빈", "스튜어트", "데이브", "칼", "제리")),
        new IPBrand("마블", "Marvel", Arrays.asList("아이언맨", "캡틴아메리카", "토르", "헐크", "스파이더맨"))
    );
    
    // 굿즈 타입
    private static final List<GoodsType> GOODS_TYPES = Arrays.asList(
        new GoodsType("피규어", Arrays.asList("넨도로이드", "팝업 퍼레이드", "프라이즈", "일반"), 15000, 150000),
        new GoodsType("인형", Arrays.asList("봉제인형", "쿠션", "키링인형", "미니인형"), 8000, 80000),
        new GoodsType("키링", Arrays.asList("아크릴", "메탈", "러버", "봉제"), 5000, 25000),
        new GoodsType("스티커", Arrays.asList("다이컷", "홀로그램", "투명", "글리터"), 2000, 15000),
        new GoodsType("문구", Arrays.asList("노트", "펜", "연필", "지우개"), 3000, 20000),
        new GoodsType("머그컵", Arrays.asList("세라믹", "스테인리스", "변색", "텀블러"), 8000, 35000),
        new GoodsType("의류", Arrays.asList("티셔츠", "후드", "맨투맨", "파자마"), 10000, 65000),
        new GoodsType("액세서리", Arrays.asList("목걸이", "팔찌", "반지", "브로치"), 5000, 45000),
        new GoodsType("전자제품", Arrays.asList("폰케이스", "에어팟케이스", "그립톡", "충전기"), 10000, 80000),
        new GoodsType("홈데코", Arrays.asList("포스터", "액자", "쿠션", "담요"), 15000, 90000)
    );
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 이미 데이터가 있으면 스킵
        if (brandRepository.count() > 0) {
            log.info("이미 데이터가 존재합니다. 데이터 생성을 건너뜁니다.");
            return;
        }
        
        log.info("========================================");
        log.info("굿즈샵 초기 데이터 생성 시작");
        log.info("========================================");
        
        long startTime = System.currentTimeMillis();
        
        // 1. 브랜드(IP) 생성
        List<BrandEntity> brands = createBrands();
        log.info("✓ {}개 브랜드(IP) 생성 완료", brands.size());
        
        // 2. 회원 생성
        List<UserEntity> users = createUsers();
        log.info("✓ {}명 회원 생성 완료", users.size());
        
        // 3. 포인트 초기화
        createPoints(users);
        log.info("✓ 회원별 포인트 초기화 완료");
        
        // 4. 상품 생성 (10만개)
        createProducts(brands);
        log.info("✓ {}개 상품 생성 완료", PRODUCT_COUNT);
        
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        
        log.info("========================================");
        log.info("굿즈샵 초기 데이터 생성 완료!");
        log.info("- 브랜드: {}개", brands.size());
        log.info("- 회원: {}명", users.size());
        log.info("- 상품: {}개", PRODUCT_COUNT);
        log.info("- 소요 시간: {}초", duration);
        log.info("========================================");
    }
    
    private List<BrandEntity> createBrands() {
        List<BrandEntity> brands = new ArrayList<>();
        
        for (IPBrand ip : IP_BRANDS) {
            BrandEntity brand = new BrandEntity(
                ip.nameKo,
                ip.nameEn,
                String.format("https://goods-shop.com/brands/%s/cover.jpg", 
                    ip.nameEn.toLowerCase().replace(" ", "-")),
                String.format("https://goods-shop.com/brands/%s/profile.jpg", 
                    ip.nameEn.toLowerCase().replace(" ", "-"))
            );
            brands.add(brandRepository.save(brand));
        }
        
        return brands;
    }
    
    private List<UserEntity> createUsers() {
        List<UserEntity> users = new ArrayList<>();
        
        // 테스트용 기본 회원 2명
        users.add(userRepository.save(new UserEntity(
            "test001",
            "테스트유저1",
            UserEntity.Gender.M,
            "1990-01-01",
            "test001@goodsshop.com"
        )));
        
        users.add(userRepository.save(new UserEntity(
            "test002",
            "테스트유저2",
            UserEntity.Gender.F,
            "1995-05-15",
            "test002@goodsshop.com"
        )));
        
        // 랜덤 회원 98명 추가
        for (int i = 3; i <= USER_COUNT; i++) {
            String userId = String.format("user%03d", i);
            String name = faker.name().fullName();
            UserEntity.Gender gender = random.nextBoolean() ? UserEntity.Gender.M : UserEntity.Gender.F;
            
            // 생년월일 (1970-2005년생)
            int year = 1970 + random.nextInt(36);
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);
            String birth = String.format("%04d-%02d-%02d", year, month, day);
            
            String email = String.format("%s@goodsshop.com", userId);
            
            UserEntity user = new UserEntity(userId, name, gender, birth, email);
            users.add(userRepository.save(user));
        }
        
        return users;
    }
    
    private void createPoints(List<UserEntity> users) {
        for (UserEntity user : users) {
            // 포인트 잔액 (0 ~ 500,000원)
            int pointBalance = random.nextInt(501) * 1000;
            
            PointEntity point = new PointEntity(
                user.getUserId(),
                Money.of(BigDecimal.valueOf(pointBalance))
            );
            
            pointRepository.save(point);
        }
    }
    
    private void createProducts(List<BrandEntity> brands) {
        log.info("상품 {}개 생성 시작...", PRODUCT_COUNT);
        
        int batchSize = 1000;
        List<ProductEntity> productBatch = new ArrayList<>();
        List<ProductStockEntity> stockBatch = new ArrayList<>();
        
        for (int i = 0; i < PRODUCT_COUNT; i++) {
            // 랜덤 브랜드(IP) 선택
            BrandEntity brand = brands.get(random.nextInt(brands.size()));
            IPBrand ipBrand = IP_BRANDS.stream()
                .filter(ip -> ip.nameKo.equals(brand.getNameKo()))
                .findFirst()
                .orElse(IP_BRANDS.get(0));
            
            // 랜덤 굿즈 타입 선택
            GoodsType goodsType = GOODS_TYPES.get(random.nextInt(GOODS_TYPES.size()));
            String subType = goodsType.subTypes.get(random.nextInt(goodsType.subTypes.size()));
            
            // 랜덤 캐릭터 선택
            String character = ipBrand.characters.get(random.nextInt(ipBrand.characters.size()));
            
            // 상품명 생성
            String productName = String.format("[%s] %s %s (%s)", 
                ipBrand.nameKo, character, goodsType.name, subType);
            
            // 가격 생성
            int basePrice = random.nextInt(goodsType.maxPrice - goodsType.minPrice) + goodsType.minPrice;
            basePrice = (basePrice / 1000) * 1000; // 1000원 단위로 반올림
            
            // 배송비 (3만원 이상 무료)
            int shippingFee = basePrice >= 30000 ? 0 : 2500;
            
            // 상품 설명
            String description = String.format("%s의 인기 캐릭터 %s %s입니다. 정품 라이선스 제품입니다.", 
                ipBrand.nameKo, character, goodsType.name);
            
            // 상태 (95% AVAILABLE, 3% OUT_OF_STOCK, 2% DISCONTINUED)
            ProductStatus status = generateStatus();
            
            // 출시년도 다양하게 분포 (2020-2024)
            Integer releaseYear = generateReleaseYear();
            
            // 상품 엔티티 생성
            ProductEntity product = new ProductEntity(
                brand.getId(),
                productName,
                Money.of(BigDecimal.valueOf(basePrice)),
                description,
                status,
                releaseYear,
                Money.of(BigDecimal.valueOf(shippingFee))
            );
            
            productBatch.add(product);
            
            // 배치 저장
            if (productBatch.size() >= batchSize || i == PRODUCT_COUNT - 1) {
                List<ProductEntity> savedProducts = productRepository.saveAll(productBatch);
                
                // 재고 정보 생성
                for (ProductEntity savedProduct : savedProducts) {
                    int stockQuantity = generateStockQuantity(savedProduct.getStatus());
                    ProductStockEntity stock = new ProductStockEntity(
                        savedProduct.getId(),
                        stockQuantity
                    );
                    stockBatch.add(stock);
                }
                
                stockRepository.saveAll(stockBatch);
                
                if ((i + 1) % 10000 == 0) {
                    log.info("  {}개 상품 생성 진행중...", i + 1);
                }
                
                productBatch.clear();
                stockBatch.clear();
            }
        }
    }
    
    private ProductStatus generateStatus() {
        int rand = random.nextInt(100);
        if (rand < 95) return ProductStatus.AVAILABLE;
        if (rand < 98) return ProductStatus.OUT_OF_STOCK;
        return ProductStatus.DISCONTINUED;
    }
    
    private Integer generateReleaseYear() {
        // 2020-2024년 분포
        // 2024년: 40%, 2023년: 30%, 2022년: 15%, 2021년: 10%, 2020년: 5%
        int rand = random.nextInt(100);
        if (rand < 40) return 2024;
        if (rand < 70) return 2023;
        if (rand < 85) return 2022;
        if (rand < 95) return 2021;
        return 2020;
    }
    
    private int generateStockQuantity(ProductStatus status) {
        if (status == ProductStatus.OUT_OF_STOCK) return 0;
        if (status == ProductStatus.DISCONTINUED) return random.nextInt(5);
        
        // AVAILABLE인 경우 다양한 재고 분포
        int rand = random.nextInt(100);
        if (rand < 10) return random.nextInt(10); // 10% 적은 재고 (0-9)
        if (rand < 30) return 10 + random.nextInt(40); // 20% 중간 재고 (10-49)
        if (rand < 70) return 50 + random.nextInt(150); // 40% 보통 재고 (50-199)
        if (rand < 90) return 200 + random.nextInt(300); // 20% 많은 재고 (200-499)
        return 500 + random.nextInt(500); // 10% 대량 재고 (500-999)
    }
    
    // 내부 클래스들
    private record IPBrand(String nameKo, String nameEn, List<String> characters) {}
    private record GoodsType(String name, List<String> subTypes, int minPrice, int maxPrice) {}
}
