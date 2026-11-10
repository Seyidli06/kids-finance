package com.kidsfinance.gamification.dto.response;

import java.time.OffsetDateTime;

public record AchievementResponse(
        String code,
        String title,
        String description,
        boolean earned,
        OffsetDateTime earnedAt
) {
}