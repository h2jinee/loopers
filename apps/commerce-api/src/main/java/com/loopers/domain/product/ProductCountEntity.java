package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name = "product_counts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCountEntity extends BaseEntity {
    
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;
    
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    
    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;
    
    @Column(name = "last_updated_at", nullable = false)
    private ZonedDateTime lastUpdatedAt;
    
    @Version
    private Long version;
    
    public ProductCountEntity(Long productId) {
        this.productId = productId;
        this.likeCount = 0L;
        this.orderCount = 0L;
        this.lastUpdatedAt = ZonedDateTime.now();
    }
    
    public void updateLikeCount(Long count) {
        this.likeCount = count != null ? count : 0L;
        this.lastUpdatedAt = ZonedDateTime.now();
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
        this.lastUpdatedAt = ZonedDateTime.now();
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
        this.lastUpdatedAt = ZonedDateTime.now();
    }
}
