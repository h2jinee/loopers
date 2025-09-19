package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Ranking API", description = "랭킹 조회 API")
public interface RankingV1ApiSpec {

    @Operation(
        summary = "랭킹 조회",
        description = "일간/주간/월간 상품 랭킹을 조회합니다."
    )
    ApiResponse<List<RankingDto.V1.GetList.Response>> getRankings(
        @Parameter(
            description = "조회 기간",
            required = true,
            example = "daily",
            schema = @Schema(allowableValues = {"daily", "weekly", "monthly"})
        )
        String period,

        @Parameter(description = "조회할 날짜", required = true, example = "20250918")
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,

        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        Integer page,

        @Parameter(description = "페이지 크기", example = "20")
        Integer size
    );
}
