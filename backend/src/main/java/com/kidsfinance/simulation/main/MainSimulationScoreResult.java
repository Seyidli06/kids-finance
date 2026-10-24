package com.kidsfinance.simulation.main;

public record MainSimulationScoreResult(
        int savingScore,
        int budgetingScore,
        int riskManagementScore,
        int investmentScore,
        int decisionMakingScore
) {
}