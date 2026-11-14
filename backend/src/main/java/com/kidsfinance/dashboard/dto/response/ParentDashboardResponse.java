package com.kidsfinance.dashboard.dto.response;

import com.kidsfinance.gamification.dto.response.StreakResponse;
import com.kidsfinance.scoring.SkillType;
import com.kidsfinance.scoring.dto.response.SkillScoreResponse;

import java.util.List;

public record ParentDashboardResponse(

        Long childId,
        String displayName,

        Integer xp,
        Integer level,

        Integer xpInCurrentLevel,
        Integer xpToNextLevel,

        Integer overallScore,

        List<SkillScoreResponse> skillScores,

        StreakResponse streak,

        Long completedChallenges,
        Long completedSimulations,
        Long completedActivities,

        Long earnedAchievements,

        List<SkillType> areasForImprovement

) {
}