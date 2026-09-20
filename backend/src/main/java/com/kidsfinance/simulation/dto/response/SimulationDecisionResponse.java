package com.kidsfinance.simulation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SimulationDecisionResponse(
        Long id,
        String scenarioCode,
        String optionCode,
        BigDecimal amountSpent,
        Long walletTransactionId,
        Integer xpEarned,
        OffsetDateTime createdAt
) {
}