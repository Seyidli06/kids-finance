package com.kidsfinance.scoring.dto.response;

public record ChildProgressResponse(
        Long childId,
        String displayName,
        Integer xp,
        Integer level,
        Integer xpInCurrentLevel,
        Integer xpPerLevel,
        Integer xpToNextLevel
) {
}