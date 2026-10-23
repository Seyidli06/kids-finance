package com.kidsfinance.scoring;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildSkillScoreRepository
        extends JpaRepository<ChildSkillScore, Long> {

    List<ChildSkillScore> findAllByChild_Id(
            Long childProfileId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM ChildSkillScore s
            WHERE s.child.id = :childProfileId
              AND s.skillType = :skillType
            """)
    Optional<ChildSkillScore> findForUpdate(
            @Param("childProfileId")
            Long childProfileId,

            @Param("skillType")
            SkillType skillType
    );
}