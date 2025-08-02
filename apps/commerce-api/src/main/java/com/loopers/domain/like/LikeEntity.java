package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "likes", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeEntity extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    public LikeEntity(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
    
    @Override
    protected void guard() {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
    }
}
