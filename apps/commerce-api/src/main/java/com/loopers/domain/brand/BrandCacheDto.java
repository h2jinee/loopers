package com.loopers.domain.brand;

import java.time.ZonedDateTime;

public record BrandCacheDto(
    Long id,
    String nameKo,
    String nameEn,
    String coverImageUrl,
    String profileImageUrl,
    ZonedDateTime createdAt
) {
    public static BrandCacheDto from(BrandEntity entity) {
        return new BrandCacheDto(
            entity.getId(),
            entity.getNameKo(),
            entity.getNameEn(),
            entity.getCoverImageUrl(),
            entity.getProfileImageUrl(),
            entity.getCreatedAt()
        );
    }
    
}
