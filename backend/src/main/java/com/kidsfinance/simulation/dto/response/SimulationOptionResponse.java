package com.kidsfinance.simulation.dto.response;

import java.math.BigDecimal;

public record SimulationOptionResponse(
        String code,
        String title,
        String description,
        BigDecimal cost
) {
}