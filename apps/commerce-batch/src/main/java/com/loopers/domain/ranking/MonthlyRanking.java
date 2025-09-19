package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "mv_product_rank_monthly",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "period_start", "period_end"}))
@Getter
@NoArgsConstructor
public class MonthlyRanking extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    private Double score;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "order_count")
    private Long orderCount;

    @Column(name = "sales_quantity")
    private Long salesQuantity;

    public static MonthlyRanking create(Long productId, LocalDate periodStart, LocalDate periodEnd,
        Double score, Long likeCount, Long orderCount, Long salesQuantity) {
        MonthlyRanking entity = new MonthlyRanking();
        entity.productId = productId;
        entity.periodStart = periodStart;
        entity.periodEnd = periodEnd;
        entity.score = score;
        entity.likeCount = likeCount;
        entity.orderCount = orderCount;
        entity.salesQuantity = salesQuantity;
        return entity;
    }
}
