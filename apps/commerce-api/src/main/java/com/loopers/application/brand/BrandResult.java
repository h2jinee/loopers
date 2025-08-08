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
        public static Detail from(BrandInfo.Detail domainInfo) {
            return new Detail(
                domainInfo.brandId(),
                domainInfo.nameKo(),
                domainInfo.nameEn(),
                domainInfo.coverImageUrl(),
                domainInfo.profileImageUrl(),
                domainInfo.productCount()
            );
        }
    }
    
    public record Summary(
        Long brandId,
        String nameKo,
        String nameEn,
        String profileImageUrl
    ) {
        public static Summary from(BrandInfo.Summary domainInfo) {
            return new Summary(
                domainInfo.brandId(),
                domainInfo.nameKo(),
                domainInfo.nameEn(),
                domainInfo.profileImageUrl()
            );
        }
    }
}