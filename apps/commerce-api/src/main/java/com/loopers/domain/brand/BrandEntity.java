package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "brands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandEntity extends BaseEntity {
    
    @Column(nullable = false)
    private String nameKo;
    
    @Column(nullable = false)
    private String nameEn;
    
    private String coverImageUrl;
    
    private String profileImageUrl;
    
    public BrandEntity(
        String nameKo,
        String nameEn,
        String coverImageUrl,
        String profileImageUrl
    ) {
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.coverImageUrl = coverImageUrl;
        this.profileImageUrl = profileImageUrl;
    }
}
