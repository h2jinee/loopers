package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingCriteria;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @GetMapping
    @Override
    public ApiResponse<List<RankingDto.V1.GetList.Response>> getRankings(
		@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        RankingCriteria criteria = new RankingCriteria(date, page, size);
        List<RankingResult> rankings = rankingFacade.getRankingsWithProducts(criteria);

        List<RankingDto.V1.GetList.Response> response = rankings.stream()
            .map(RankingDto.V1.GetList.Response::from)
            .toList();

        return ApiResponse.success(response);
    }
}
