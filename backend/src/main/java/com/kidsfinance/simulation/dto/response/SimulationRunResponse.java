package com.kidsfinance.simulation.dto.response;

import com.kidsfinance.simulation.SimulationEventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SimulationRunResponse(

        Long id,

        BigDecimal startingAmount,

        BigDecimal cash,
        BigDecimal saving,
        BigDecimal deposit,
        BigDecimal bond,
        BigDecimal investment,
        BigDecimal spending,
        BigDecimal business,

        BigDecimal portfolioValueBeforeEvent,

        SimulationEventType eventType,
        String eventTitle,
        String eventDescription,

        BigDecimal eventDelta,
        BigDecimal endingAmount,
        BigDecimal netChange,

        Integer savingScore,
        Integer budgetingScore,
        Integer riskManagementScore,
        Integer investmentScore,
        Integer decisionMakingScore,

        Integer xpEarned,

        OffsetDateTime createdAt

) {
}