package com.kidsfinance.scoring.dto.response;

import com.kidsfinance.scoring.SkillType;

public record SkillScoreResponse(
        SkillType skill,
        Integer score,
        Integer activityCount
) {
}