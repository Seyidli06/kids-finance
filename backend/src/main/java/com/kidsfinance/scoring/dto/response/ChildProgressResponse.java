package com.kidsfinance.scoring.dto.response;

import java.util.List;

public record ChildProgressResponse(
        Long childId,
        String displayName,
        Integer xp,
        Integer level,
        Integer xpInCurrentLevel,
        Integer xpPerLevel,
        Integer xpToNextLevel,
        Integer overallScore,
        List<SkillScoreResponse> skillScores
) {
}