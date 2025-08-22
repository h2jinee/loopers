package com.loopers.domain.brand;

public record BrandInfo(
    Long brandId,
    String nameKo,
    String nameEn,
    String coverImageUrl,
    String profileImageUrl
) {
    public static BrandInfo from(Brand brand) {
        return new BrandInfo(
            brand.getId(),
            brand.getNameKo(),
            brand.getNameEn(),
            brand.getCoverImageUrl(),
            brand.getProfileImageUrl()
        );
    }
}
