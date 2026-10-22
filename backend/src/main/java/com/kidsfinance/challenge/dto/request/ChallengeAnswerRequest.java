package com.kidsfinance.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChallengeAnswerRequest(
        @NotBlank String optionCode
) {
}