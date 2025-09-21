package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRanking, Long> {

    @Query("""
        SELECT m FROM WeeklyRanking m
        WHERE m.periodEnd = :date
        ORDER BY m.score DESC
        """)
    List<WeeklyRanking> findByPeriodEnd(
        @Param("date") LocalDate date,
        Pageable pageable
    );
}
