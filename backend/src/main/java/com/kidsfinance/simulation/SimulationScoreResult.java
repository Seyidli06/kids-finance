package com.kidsfinance.simulation;

public record SimulationScoreResult(
        int savingScore,
        int budgetingScore,
        int riskManagementScore,
        int investmentScore,
        int decisionMakingScore
) {
}