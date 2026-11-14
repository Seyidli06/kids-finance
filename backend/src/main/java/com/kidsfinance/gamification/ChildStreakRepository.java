package com.kidsfinance.gamification;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChildStreakRepository
        extends JpaRepository<ChildStreak, Long> {

    boolean existsByChild_Id(Long childProfileId);

    Optional<ChildStreak> findByChild_Id(
            Long childProfileId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM ChildStreak s
            WHERE s.child.id = :childProfileId
            """)
    Optional<ChildStreak> findByChildIdForUpdate(
            @Param("childProfileId")
            Long childProfileId
    );
}