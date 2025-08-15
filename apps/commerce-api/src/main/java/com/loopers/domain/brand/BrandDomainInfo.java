package com.loopers.domain.brand;

import java.time.ZonedDateTime;

public record BrandDomainInfo(
    Long id,
    String nameKo,
    String nameEn,
    String coverImageUrl,
    String profileImageUrl,
    ZonedDateTime createdAt
) {
    public static BrandDomainInfo from(BrandEntity entity) {
        return new BrandDomainInfo(
            entity.getId(),
            entity.getNameKo(),
            entity.getNameEn(),
            entity.getCoverImageUrl(),
            entity.getProfileImageUrl(),
            entity.getCreatedAt()
        );
    }
    
    public static BrandDomainInfo from(BrandCacheDto cacheDto) {
        return new BrandDomainInfo(
            cacheDto.id(),
            cacheDto.nameKo(),
            cacheDto.nameEn(),
            cacheDto.coverImageUrl(),
            cacheDto.profileImageUrl(),
            cacheDto.createdAt()
        );
    }
}
