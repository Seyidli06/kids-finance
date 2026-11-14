package com.kidsfinance.gamification.dto.response;

import java.time.LocalDate;

public record StreakResponse(
        Integer currentStreak,
        Integer longestStreak,
        LocalDate lastActivityDate,
        boolean activeToday
) {
}