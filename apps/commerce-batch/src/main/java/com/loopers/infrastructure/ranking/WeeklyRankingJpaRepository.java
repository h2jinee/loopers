package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRanking, Long> {

}
