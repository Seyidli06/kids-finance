package com.kidsfinance.simulation.main;

import com.kidsfinance.simulation.main.dto.request.MainSimulationRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MainSimulationScoringService {

    private static final BigDecimal SAVING_TARGET =
            new BigDecimal("300.00");

    private static final BigDecimal MEANINGFUL_INVESTMENT =
            new BigDecimal("50.00");

    public MainSimulationScoreResult calculate(
            MainSimulationRequest request
    ) {

        int savingScore =
                calculateSavingScore(request);

        int budgetingScore =
                calculateBudgetingScore(request);

        int riskManagementScore =
                calculateRiskManagementScore(request);

        int investmentScore =
                calculateInvestmentScore(request);

        int decisionMakingScore =
                Math.round(
                        (
                                savingScore
                                        + budgetingScore
                                        + riskManagementScore
                                        + investmentScore
                        ) / 4.0f
                );

        return new MainSimulationScoreResult(
                savingScore,
                budgetingScore,
                riskManagementScore,
                investmentScore,
                decisionMakingScore
        );
    }

    private int calculateSavingScore(
            MainSimulationRequest request
    ) {

        BigDecimal protectedMoney =
                request.saving()
                        .add(
                                request.deposit()
                        );

        if (protectedMoney.compareTo(
                SAVING_TARGET
        ) >= 0) {
            return 100;
        }

        return protectedMoney
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        SAVING_TARGET,
                        0,
                        RoundingMode.HALF_UP
                )
                .intValue();
    }

    private int calculateBudgetingScore(
            MainSimulationRequest request
    ) {

        int score = 0;

        BigDecimal spending =
                request.spending();

        if (spending.compareTo(
                new BigDecimal("200")
        ) <= 0) {
            score += 40;
        } else if (spending.compareTo(
                new BigDecimal("300")
        ) <= 0) {
            score += 30;
        } else if (spending.compareTo(
                new BigDecimal("400")
        ) <= 0) {
            score += 20;
        } else if (spending.compareTo(
                new BigDecimal("500")
        ) <= 0) {
            score += 10;
        }

        BigDecimal liquidReserve =
                request.cash()
                        .add(
                                request.saving()
                        );

        if (liquidReserve.compareTo(
                new BigDecimal("200")
        ) >= 0) {
            score += 40;
        } else if (liquidReserve.compareTo(
                new BigDecimal("100")
        ) >= 0) {
            score += 20;
        }

        int activeAllocations =
                countPositiveAllocations(
                        request
                );

        if (activeAllocations >= 4) {
            score += 20;
        } else if (activeAllocations >= 2) {
            score += 10;
        }

        return Math.min(
                score,
                100
        );
    }

    private int calculateRiskManagementScore(
            MainSimulationRequest request
    ) {

        BigDecimal maximumAllocation =
                List.of(
                                request.cash(),
                                request.saving(),
                                request.deposit(),
                                request.bond(),
                                request.investment(),
                                request.spending(),
                                request.business()
                        )
                        .stream()
                        .max(
                                BigDecimal::compareTo
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );

        int concentrationScore;

        if (maximumAllocation.compareTo(
                new BigDecimal("400")
        ) <= 0) {
            concentrationScore = 50;
        } else if (maximumAllocation.compareTo(
                new BigDecimal("600")
        ) <= 0) {
            concentrationScore = 30;
        } else if (maximumAllocation.compareTo(
                new BigDecimal("800")
        ) <= 0) {
            concentrationScore = 15;
        } else {
            concentrationScore = 0;
        }

        BigDecimal reserve =
                request.cash()
                        .add(request.saving())
                        .add(request.deposit());

        int reserveScore;

        if (reserve.compareTo(
                new BigDecimal("300")
        ) >= 0) {
            reserveScore = 50;
        } else if (reserve.compareTo(
                new BigDecimal("200")
        ) >= 0) {
            reserveScore = 35;
        } else if (reserve.compareTo(
                new BigDecimal("100")
        ) >= 0) {
            reserveScore = 20;
        } else {
            reserveScore = 0;
        }

        return concentrationScore
                + reserveScore;
    }

    private int calculateInvestmentScore(
            MainSimulationRequest request
    ) {

        int meaningfulChoices = 0;

        if (isMeaningful(
                request.deposit()
        )) {
            meaningfulChoices++;
        }

        if (isMeaningful(
                request.bond()
        )) {
            meaningfulChoices++;
        }

        if (isMeaningful(
                request.investment()
        )) {
            meaningfulChoices++;
        }

        if (isMeaningful(
                request.business()
        )) {
            meaningfulChoices++;
        }

        return meaningfulChoices * 25;
    }

    private boolean isMeaningful(
            BigDecimal amount
    ) {

        return amount.compareTo(
                MEANINGFUL_INVESTMENT
        ) >= 0;
    }

    private int countPositiveAllocations(
            MainSimulationRequest request
    ) {

        return (int) List.of(
                        request.cash(),
                        request.saving(),
                        request.deposit(),
                        request.bond(),
                        request.investment(),
                        request.spending(),
                        request.business()
                )
                .stream()
                .filter(amount ->
                        amount.signum() > 0
                )
                .count();
    }
}