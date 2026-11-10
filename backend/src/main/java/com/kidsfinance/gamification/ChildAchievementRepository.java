package com.kidsfinance.gamification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildAchievementRepository
        extends JpaRepository<ChildAchievement, Long> {

    boolean existsByChild_IdAndAchievement_Id(
            Long childProfileId,
            Long achievementId
    );

    List<ChildAchievement>
    findAllByChild_Id(
            Long childProfileId
    );
}