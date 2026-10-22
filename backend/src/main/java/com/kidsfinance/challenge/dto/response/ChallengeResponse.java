package com.kidsfinance.challenge.dto.response;

import java.util.List;

public record ChallengeResponse(
        String code,
        String topicCode,
        String title,
        String prompt,
        Integer xpReward,
        boolean completed,
        List<ChallengeOptionResponse> options
) {
}