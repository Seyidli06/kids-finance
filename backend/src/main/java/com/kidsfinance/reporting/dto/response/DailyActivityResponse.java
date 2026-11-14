package com.kidsfinance.reporting.dto.response;

import java.time.LocalDate;

public record DailyActivityResponse(
        LocalDate date,
        Long challengeAttempts,
        Long correctChallengeAnswers,
        Long simulationRuns,
        Long totalActivities,
        Integer xpEarned
) {
}