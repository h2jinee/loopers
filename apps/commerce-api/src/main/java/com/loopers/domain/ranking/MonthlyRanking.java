package com.loopers.domain.ranking;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(name = "mv_product_rank_monthly")
@Getter
public class MonthlyRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}
