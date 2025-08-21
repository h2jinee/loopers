package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.product.vo.ProductStatus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "products", indexes = {
    @Index(name = "idx_products_brand_id", columnList = "brand_id"),
    @Index(name = "idx_products_brand_created", columnList = "brand_id, created_at DESC"),
    @Index(name = "idx_products_brand_like", columnList = "brand_id, like_count DESC"),
    @Index(name = "idx_products_like_desc", columnList = "like_count DESC"),
    @Index(name = "idx_products_brand_id_pk", columnList = "brand_id, id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    
    @Column(nullable = false)
    private Long brandId;
    
    @Column(nullable = false)
    private String nameKo;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
    
    private Integer releaseYear;
    
    @Column(nullable = false)
    private BigDecimal shippingFee;
    
    // 비정규화 필드
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    public Product(
        Long brandId,
        String nameKo,
        Money price,
        String description,
        ProductStatus status,
        Integer releaseYear,
        Money shippingFee
    ) {
        this.brandId = brandId;
        this.nameKo = nameKo;
        this.price = price.amount();
        this.description = description;
        this.status = status;
        this.releaseYear = releaseYear;
        this.shippingFee = shippingFee.amount();
    }

    public Money getPrice() {
        return Money.of(price);
    }
    
    public Money getShippingFee() {
        return Money.of(shippingFee);
    }
    
    public Money getTotalPrice() {
        return getPrice().add(getShippingFee());
    }

    /**
     * 좋아요 수 증가
     */
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount != null ? this.likeCount : 0L) + 1;
    }
    
    /**
     * 좋아요 수 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    /**
     * 좋아요 수 동기화 (배치 작업용)
     * @param count 동기화할 좋아요 수
     */
    public void syncLikeCount(Long count) {
        this.likeCount = count != null ? count : 0L;
    }
}
