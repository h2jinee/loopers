package com.loopers.domain.brand;

public class BrandInfo {
    
    public record Detail(
        Long brandId,
        String nameKo,
        String nameEn,
        String coverImageUrl,
        String profileImageUrl,
        Integer productCount
    ) {
        public static Detail from(Brand brand, Integer productCount) {
            return new Detail(
                brand.getId(),
                brand.getNameKo(),
                brand.getNameEn(),
                brand.getCoverImageUrl(),
                brand.getProfileImageUrl(),
                productCount != null ? productCount : 0
            );
        }
    }
    
    public record Summary(
        Long brandId,
        String nameKo,
        String nameEn,
        String profileImageUrl
    ) {
        public static Summary from(Brand brand) {
            return new Summary(
                brand.getId(),
                brand.getNameKo(),
                brand.getNameEn(),
                brand.getProfileImageUrl()
            );
        }
    }
}
