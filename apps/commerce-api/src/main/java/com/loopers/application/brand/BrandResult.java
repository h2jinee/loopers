package com.loopers.application.brand;

import com.loopers.domain.brand.BrandInfo;

public class BrandResult {
    
    public record Detail(
        Long brandId,
        String nameKo,
        String nameEn,
        String coverImageUrl,
        String profileImageUrl,
        Integer productCount
    ) {
        public static Detail from(BrandInfo brandInfo, Integer productCount) {
            return new Detail(
                brandInfo.brandId(),
                brandInfo.nameKo(),
                brandInfo.nameEn(),
                brandInfo.coverImageUrl(),
                brandInfo.profileImageUrl(),
                productCount
            );
        }
    }
    
    public record Summary(
        Long brandId,
        String nameKo,
        String nameEn,
        String profileImageUrl
    ) {
        public static Summary from(BrandInfo brandInfo) {
            return new Summary(
                brandInfo.brandId(),
                brandInfo.nameKo(),
                brandInfo.nameEn(),
                brandInfo.profileImageUrl()
            );
        }
    }
}
