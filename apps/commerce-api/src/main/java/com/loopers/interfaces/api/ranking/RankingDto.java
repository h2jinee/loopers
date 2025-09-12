package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class RankingDto {

    public static class V1 {

        public static class GetList {

            @Builder
            @Schema(description = "랭킹 정보")
            public record Response(
                @Schema(description = "순위", example = "1")
                Integer rank,

                @Schema(description = "상품 ID", example = "100650")
                Long productId,

                @Schema(description = "상품명", example = "동물의 숲 쭈니 키링")
                String productName,

                @Schema(description = "가격", example = "150000")
                Long price,

                @Schema(description = "브랜드 ID", example = "10")
                Long brandId,

                @Schema(description = "랭킹 점수", example = "1234.56")
                Double score
            ) {
                public static Response from(RankingResult result) {
                    return Response.builder()
                        .rank(result.rank())
                        .productId(result.productId())
                        .productName(result.productName())
                        .price(result.price())
                        .brandId(result.brandId())
                        .score(result.score())
                        .build();
                }
            }
        }
    }
}
