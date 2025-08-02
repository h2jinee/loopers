package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class BrandDto {
    
    public static class V1 {
        
        public static class GetList {
            public record Request(
                @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
                Integer page,
                
                @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
                Integer size
            ) {}
            
            public record Response(
                Long brandId,
                String nameKo,
                String nameEn,
                String profileImageUrl
            ) {
                public static Response from(BrandInfo.Summary summary) {
                    return new Response(
                        summary.brandId(),
                        summary.nameKo(),
                        summary.nameEn(),
                        summary.profileImageUrl()
                    );
                }
            }
        }
        
        public static class GetDetail {
            public record Response(
                Long brandId,
                String nameKo,
                String nameEn,
                String coverImageUrl,
                String profileImageUrl,
                Integer productCount
            ) {
                public static Response from(BrandInfo.Detail detail) {
                    return new Response(
                        detail.brandId(),
                        detail.nameKo(),
                        detail.nameEn(),
                        detail.coverImageUrl(),
                        detail.profileImageUrl(),
                        detail.productCount()
                    );
                }
            }
        }
    }
}
