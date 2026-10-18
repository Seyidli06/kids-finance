package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.scoring.ScoringService;
import com.kidsfinance.simulation.dto.request.SimulationDecisionRequest;
import com.kidsfinance.simulation.dto.response.SimulationDecisionResponse;
import com.kidsfinance.simulation.dto.response.SimulationOptionResponse;
import com.kidsfinance.simulation.dto.response.SimulationRunResponse;
import com.kidsfinance.simulation.dto.response.SimulationScenarioResponse;
import com.kidsfinance.wallet.WalletService;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationScenarioRepository simulationScenarioRepository;
    private final SimulationOptionRepository simulationOptionRepository;
    private final SimulationDecisionRepository simulationDecisionRepository;
    private final SimulationRunRepository simulationRunRepository;

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ChildProfileRepository childProfileRepository;

    private final WalletService walletService;
    private final ScoringService scoringService;

    // =========================
    // Parent API
    // =========================

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        return getActiveScenarios();
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

        ChildProfile child =
                getChildByIdForParent(
                        childProfileId
                );

        return processDecision(
                child,
                scenarioCode,
                request
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

        return getDecisionHistory(
                childProfileId
        );
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

        return getRunHistory(
                childProfileId
        );
    }

    // =========================
    // Child API
    // =========================

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForChild(
            Long childUserId
    ) {

        getChildByUserId(childUserId);

        return getActiveScenarios();
    }

    @Transactional
    public SimulationDecisionResponse makeDecisionForChild(
            Long childUserId,
            String scenarioCode,
            SimulationDecisionRequest request
    ) {

        ChildProfile child =
                getChildByUserId(
                        childUserId
                );

        return processDecision(
                child,
                scenarioCode,
                request
        );
    }

    @Transactional(readOnly = true)
    public List<SimulationDecisionResponse> getDecisionsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(
                        childUserId
                );

        return getDecisionHistory(
                child.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<SimulationRunResponse> getRunsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(
                        childUserId
                );

        return getRunHistory(
                child.getId()
        );
    }

    // =========================
    // Shared business logic
    // =========================

    private SimulationDecisionResponse processDecision(
            ChildProfile child,
            String scenarioCode,
            SimulationDecisionRequest request
    ) {

        SimulationScenario scenario =
                simulationScenarioRepository
                        .findByCodeAndActiveTrue(
                                scenarioCode
                        )
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

        BigDecimal amountSpent =
                selectedOption.getCost();

        Long walletTransactionId = null;

        if (amountSpent.signum() > 0) {

            WalletTransactionResponse walletTransaction =
                    walletService.debitForSimulation(
                            child.getId(),
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
                        .awardScenarioCompletionXp(
                                child
                        );

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

        return toDecisionResponse(
                savedDecision
        );
    }

    private List<SimulationScenarioResponse> getActiveScenarios() {

        return simulationScenarioRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toScenarioResponse)
                .toList();
    }

    private List<SimulationDecisionResponse> getDecisionHistory(
            Long childProfileId
    ) {

        return simulationDecisionRepository
                .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                        childProfileId
                )
                .stream()
                .map(this::toDecisionResponse)
                .toList();
    }

    private List<SimulationRunResponse> getRunHistory(
            Long childProfileId
    ) {

        return simulationRunRepository
                .findAllByChild_IdOrderByStartedAtDescIdDesc(
                        childProfileId
                )
                .stream()
                .map(this::toRunResponse)
                .toList();
    }

    private ChildProfile getChildByIdForParent(
            Long childProfileId
    ) {

        return childProfileRepository
                .findById(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child not found for authenticated parent"
                        )
                );
    }

    private ChildProfile getChildByUserId(
            Long childUserId
    ) {

        return childProfileRepository
                .findByUser_Id(childUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child profile not found for authenticated user"
                        )
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

    // =========================
    // Mapping
    // =========================

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

    private SimulationDecisionResponse toDecisionResponse(
            SimulationDecision decision
    ) {

        return new SimulationDecisionResponse(
                decision.getId(),
                decision.getRun().getId(),
                decision.getScenarioCode(),
                decision.getOptionCode(),
                decision.getAmountSpent(),
                decision.getWalletTransactionId(),
                decision.getXpEarned(),
                decision.getCreatedAt()
        );
    }

    private SimulationRunResponse toRunResponse(
            SimulationRun run
    ) {

        return new SimulationRunResponse(
                run.getId(),
                run.getScenario().getCode(),
                run.getStatus(),
                run.getStartedAt(),
                run.getCompletedAt()
        );
    }
}