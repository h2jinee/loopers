package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyRankingJpaRepository extends JpaRepository<MonthlyRanking, Long> {

}
