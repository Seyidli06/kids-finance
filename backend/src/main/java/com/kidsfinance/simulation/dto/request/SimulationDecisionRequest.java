package com.kidsfinance.simulation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SimulationDecisionRequest(
        @NotBlank String optionCode
) {
}