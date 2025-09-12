package com.loopers.application.ranking;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingInfo;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;

    /**
     * 랭킹 조회 (상품 정보 포함)
     */
    public List<RankingResult> getRankingsWithProducts(RankingCriteria criteria) {
        // 1. 랭킹 조회
        List<RankingInfo> rankings = rankingService.getRankings(
            new RankingCommand.GetList(
                criteria.date(),
                criteria.page(),
                criteria.size()
            )
        );

        if (rankings.isEmpty()) {
            return List.of();
        }

        // 2. 상품 정보 벌크 조회
        List<Long> productIds = rankings.stream()
            .map(RankingInfo::productId)
            .toList();

        Map<Long, ProductInfo> productMap = productService.getProductsByIds(productIds);

        // 3. 결과 조합
        return rankings.stream()
            .map(ranking -> {
                ProductInfo product = productMap.get(ranking.productId());
                return RankingResult.from(ranking, product);
            })
            .toList();
    }
}
