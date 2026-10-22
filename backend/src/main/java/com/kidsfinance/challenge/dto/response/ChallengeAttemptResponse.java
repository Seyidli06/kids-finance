package com.kidsfinance.challenge.dto.response;

import java.time.OffsetDateTime;

public record ChallengeAttemptResponse(
        Long id,
        String challengeCode,
        String selectedOptionCode,
        boolean correct,
        String explanation,
        Integer xpAwarded,
        OffsetDateTime createdAt
) {
}