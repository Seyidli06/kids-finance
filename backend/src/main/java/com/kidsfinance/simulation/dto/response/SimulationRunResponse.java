package com.kidsfinance.simulation.dto.response;

import com.kidsfinance.simulation.SimulationRunStatus;

import java.time.OffsetDateTime;

public record SimulationRunResponse(
        Long id,
        String scenarioCode,
        SimulationRunStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt
) {
}