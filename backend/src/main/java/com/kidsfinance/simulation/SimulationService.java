package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.scoring.ScoringService;
import com.kidsfinance.simulation.dto.request.SimulationDecisionRequest;
import com.kidsfinance.simulation.dto.response.SimulationDecisionResponse;
import com.kidsfinance.simulation.dto.response.SimulationOptionResponse;
import com.kidsfinance.simulation.dto.response.SimulationScenarioResponse;
import com.kidsfinance.wallet.WalletService;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import com.kidsfinance.simulation.dto.response.SimulationRunResponse;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationScenarioRepository simulationScenarioRepository;
    private final SimulationOptionRepository simulationOptionRepository;

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ChildProfileRepository childProfileRepository;
    private final SimulationDecisionRepository simulationDecisionRepository;
    private final WalletService walletService;
    private final ScoringService scoringService;

    private final SimulationRunRepository simulationRunRepository;

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        return simulationScenarioRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toScenarioResponse)
                .toList();
    }

    @Transactional
    public SimulationDecisionResponse makeDecision(
            Long parentUserId,
            Long childProfileId,
            String scenarioCode,
            SimulationDecisionRequest request
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        ChildProfile child = childProfileRepository
                .findById(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child not found for authenticated parent"
                        )
                );

        SimulationScenario scenario =
                simulationScenarioRepository
                        .findByCodeAndActiveTrue(scenarioCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Simulation scenario not found"
                                )
                        );

        SimulationOption selectedOption =
                simulationOptionRepository
                        .findByScenario_IdAndCodeAndActiveTrue(
                                scenario.getId(),
                                request.optionCode()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Simulation option not found"
                                )
                        );

        SimulationRun run =
                simulationRunRepository.save(
                        SimulationRun.builder()
                                .child(child)
                                .scenario(scenario)
                                .status(
                                        SimulationRunStatus.IN_PROGRESS
                                )
                                .build()
                );


        BigDecimal amountSpent = selectedOption.getCost();

        Long walletTransactionId = null;

        if (amountSpent.signum() > 0) {

            WalletTransactionResponse walletTransaction =
                    walletService.debit(
                            parentUserId,
                            childProfileId,
                            amountSpent,
                            "Simulation "
                                    + scenario.getCode()
                                    + ": "
                                    + selectedOption.getCode()
                    );

            walletTransactionId =
                    walletTransaction.id();
        }

        int xpEarned =
                scoringService
                        .awardScenarioCompletionXp(child);

        SimulationDecision decision =
                SimulationDecision.builder()
                        .child(child)
                        .run(run)
                        .option(selectedOption)
                        .scenarioCode(
                                scenario.getCode()
                        )
                        .optionCode(
                                selectedOption.getCode()
                        )
                        .amountSpent(
                                amountSpent
                        )
                        .walletTransactionId(
                                walletTransactionId
                        )
                        .xpEarned(
                                xpEarned
                        )
                        .build();

        SimulationDecision savedDecision =
                simulationDecisionRepository
                        .save(decision);

        run.complete();

        return new SimulationDecisionResponse(
                savedDecision.getId(),
                savedDecision.getRun().getId(),
                savedDecision.getScenarioCode(),
                savedDecision.getOptionCode(),
                savedDecision.getAmountSpent(),
                savedDecision.getWalletTransactionId(),
                savedDecision.getXpEarned(),
                savedDecision.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<SimulationDecisionResponse> getDecisionsForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        return simulationDecisionRepository
                .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                        childProfileId
                )
                .stream()
                .map(decision ->
                        new SimulationDecisionResponse(
                                decision.getId(),
                                decision.getRun().getId(),
                                decision.getScenarioCode(),
                                decision.getOptionCode(),
                                decision.getAmountSpent(),
                                decision.getWalletTransactionId(),
                                decision.getXpEarned(),
                                decision.getCreatedAt()
                        )
                )
                .toList();
    }

    private SimulationScenarioResponse toScenarioResponse(
            SimulationScenario scenario
    ) {

        List<SimulationOptionResponse> options =
                simulationOptionRepository
                        .findAllByScenario_IdAndActiveTrueOrderByDisplayOrderAsc(
                                scenario.getId()
                        )
                        .stream()
                        .map(option ->
                                new SimulationOptionResponse(
                                        option.getCode(),
                                        option.getTitle(),
                                        option.getDescription(),
                                        option.getCost()
                                )
                        )
                        .toList();

        return new SimulationScenarioResponse(
                scenario.getCode(),
                scenario.getTitle(),
                scenario.getDescription(),
                options
        );
    }

    private void verifyChildBelongsToParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent =
                parentChildLinkRepository
                        .existsByParent_User_IdAndChild_Id(
                                parentUserId,
                                childProfileId
                        );

        if (!belongsToParent) {
            throw new ResourceNotFoundException(
                    "Child not found for authenticated parent"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<SimulationRunResponse> getRunsForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        return simulationRunRepository
                .findAllByChild_IdOrderByStartedAtDescIdDesc(
                        childProfileId
                )
                .stream()
                .map(run ->
                        new SimulationRunResponse(
                                run.getId(),
                                run.getScenario().getCode(),
                                run.getStatus(),
                                run.getStartedAt(),
                                run.getCompletedAt()
                        )
                )
                .toList();
    }
}