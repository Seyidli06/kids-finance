package com.kidsfinance.simulation.dto.response;

import java.util.List;

public record SimulationScenarioResponse(
        String code,
        String title,
        String description,
        List<SimulationOptionResponse> options
) {
}