package com.ducvt.diabeater.account.repository;

import com.ducvt.diabeater.account.models.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {
    Optional<List<Stats>> findByUserId(Long userId);

    Optional<List<Stats>> findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(Long userId, Date start, Date end);

    Optional<List<Stats>> findByUserIdAndCreateTimeBetweenOrderByCreateTimeAsc(Long userId, Date start, Date end);
}
