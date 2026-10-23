package com.kidsfinance.simulation.main.dto.response;

import com.kidsfinance.simulation.main.MainSimulationEventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MainSimulationRunResponse(

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

        MainSimulationEventType eventType,
        String eventTitle,
        String eventDescription,

        BigDecimal eventDelta,
        BigDecimal endingAmount,
        BigDecimal netChange,

        Integer xpEarned,

        OffsetDateTime createdAt

) {
}