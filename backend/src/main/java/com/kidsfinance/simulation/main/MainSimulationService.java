package com.kidsfinance.simulation.main;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.scoring.ScoringService;
import com.kidsfinance.scoring.SkillScoringService;
import com.kidsfinance.scoring.SkillType;
import com.kidsfinance.simulation.main.dto.request.MainSimulationRequest;
import com.kidsfinance.simulation.main.dto.response.MainSimulationRunResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MainSimulationService {

    private static final BigDecimal STARTING_AMOUNT =
            new BigDecimal("1000.00");

    private static final BigDecimal STOCK_DROP_RATE =
            new BigDecimal("0.15");

    private static final BigDecimal DEPOSIT_RATE =
            new BigDecimal("0.06");

    private static final BigDecimal BUSINESS_RATE =
            new BigDecimal("0.20");

    private static final BigDecimal UNEXPECTED_EXPENSE =
            new BigDecimal("200.00");

    private final MainSimulationRunRepository mainSimulationRunRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;

    private final ScoringService scoringService;
    private final SkillScoringService skillScoringService;
    private final MainSimulationScoringService mainSimulationScoringService;

    // =====================================================
    // Child
    // =====================================================

    @Transactional
    public MainSimulationRunResponse runForChild(
            Long childUserId,
            MainSimulationRequest request
    ) {

        if (!request.isAllocationTotalValid()) {
            throw new IllegalArgumentException(
                    "Allocations must total exactly 1000.00 AZN"
            );
        }

        ChildProfile resolvedChild =
                childProfileRepository
                        .findByUser_Id(childUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found for authenticated user"
                                )
                        );

        /*
         * Lock the child row because this operation can update:
         *
         * - XP
         * - level
         * - skill scores
         *
         * Keeping these updates inside the same transaction prevents
         * concurrent simulation runs from losing progress updates.
         */
        ChildProfile child =
                childProfileRepository
                        .findByIdForUpdate(
                                resolvedChild.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found"
                                )
                        );

        /*
         * Main simulation can be replayed.
         *
         * XP is awarded only on the first completed run so that the child
         * cannot farm XP by repeatedly submitting the same simulation.
         */
        boolean firstRun =
                !mainSimulationRunRepository
                        .existsByChild_Id(
                                child.getId()
                        );

        // -------------------------------------------------
        // Calculate decision quality
        // -------------------------------------------------

        MainSimulationScoreResult scores =
                mainSimulationScoringService
                        .calculate(request);

        /*
         * Every simulation run is an activity, therefore it updates all
         * five financial skill dimensions.
         *
         * These scores describe the quality of the allocation decision.
         * They are intentionally calculated before / independently of the
         * random event because market luck must not determine whether the
         * child's financial decision was good or bad.
         */
        skillScoringService.recordActivity(
                child,
                SkillType.SAVING,
                scores.savingScore()
        );

        skillScoringService.recordActivity(
                child,
                SkillType.BUDGETING,
                scores.budgetingScore()
        );

        skillScoringService.recordActivity(
                child,
                SkillType.RISK_MANAGEMENT,
                scores.riskManagementScore()
        );

        skillScoringService.recordActivity(
                child,
                SkillType.INVESTMENT,
                scores.investmentScore()
        );

        skillScoringService.recordActivity(
                child,
                SkillType.DECISION_MAKING,
                scores.decisionMakingScore()
        );

        // -------------------------------------------------
        // Select event
        // -------------------------------------------------

        MainSimulationEventType event =
                selectRandomEvent();

        /*
         * Spending is treated as consumed money.
         *
         * Example:
         *
         * Start:     1000 AZN
         * Spending:   100 AZN
         *
         * Portfolio before event = 900 AZN
         */
        BigDecimal portfolioValueBeforeEvent =
                money(
                        STARTING_AMOUNT.subtract(
                                request.spending()
                        )
                );

        BigDecimal eventDelta =
                calculateEventDelta(
                        event,
                        request,
                        portfolioValueBeforeEvent
                );

        BigDecimal endingAmount =
                money(
                        portfolioValueBeforeEvent
                                .add(eventDelta)
                );

        BigDecimal netChange =
                money(
                        endingAmount.subtract(
                                STARTING_AMOUNT
                        )
                );

        // -------------------------------------------------
        // XP
        // -------------------------------------------------

        int xpEarned = 0;

        if (firstRun) {
            xpEarned =
                    scoringService
                            .awardMainSimulationCompletionXp(
                                    child
                            );
        }

        // -------------------------------------------------
        // Persist run
        // -------------------------------------------------

        MainSimulationRun run =
                MainSimulationRun.builder()
                        .child(child)

                        .startingAmount(
                                STARTING_AMOUNT
                        )

                        .cashAmount(
                                money(request.cash())
                        )

                        .savingAmount(
                                money(request.saving())
                        )

                        .depositAmount(
                                money(request.deposit())
                        )

                        .bondAmount(
                                money(request.bond())
                        )

                        .investmentAmount(
                                money(request.investment())
                        )

                        .spendingAmount(
                                money(request.spending())
                        )

                        .businessAmount(
                                money(request.business())
                        )

                        .portfolioValueBeforeEvent(
                                portfolioValueBeforeEvent
                        )

                        .eventType(
                                event
                        )

                        .eventDelta(
                                eventDelta
                        )

                        .endingAmount(
                                endingAmount
                        )

                        .netChange(
                                netChange
                        )

                        .savingScore(
                                scores.savingScore()
                        )

                        .budgetingScore(
                                scores.budgetingScore()
                        )

                        .riskManagementScore(
                                scores.riskManagementScore()
                        )

                        .investmentScore(
                                scores.investmentScore()
                        )

                        .decisionMakingScore(
                                scores.decisionMakingScore()
                        )

                        .xpEarned(
                                xpEarned
                        )

                        .build();

        MainSimulationRun savedRun =
                mainSimulationRunRepository
                        .save(run);

        return toResponse(
                savedRun
        );
    }

    @Transactional(readOnly = true)
    public List<MainSimulationRunResponse> getRunsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                childProfileRepository
                        .findByUser_Id(childUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found for authenticated user"
                                )
                        );

        return getRunHistory(
                child.getId()
        );
    }

    // =====================================================
    // Parent
    // =====================================================

    @Transactional(readOnly = true)
    public List<MainSimulationRunResponse> getRunsForParent(
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

        return getRunHistory(
                childProfileId
        );
    }

    // =====================================================
    // Shared history
    // =====================================================

    private List<MainSimulationRunResponse> getRunHistory(
            Long childProfileId
    ) {

        return mainSimulationRunRepository
                .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                        childProfileId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // Event selection
    // =====================================================

    private MainSimulationEventType selectRandomEvent() {

        MainSimulationEventType[] values =
                MainSimulationEventType.values();

        int index =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                values.length
                        );

        return values[index];
    }

    // =====================================================
    // Event calculations
    // =====================================================

    private BigDecimal calculateEventDelta(
            MainSimulationEventType event,
            MainSimulationRequest request,
            BigDecimal portfolioValueBeforeEvent
    ) {

        BigDecimal delta =
                switch (event) {

                    case STOCK_MARKET_DROP ->
                            request.investment()
                                    .multiply(
                                            STOCK_DROP_RATE
                                    )
                                    .negate();

                    case UNEXPECTED_EXPENSE ->
                            portfolioValueBeforeEvent
                                    .min(
                                            UNEXPECTED_EXPENSE
                                    )
                                    .negate();

                    case DEPOSIT_INTEREST ->
                            request.deposit()
                                    .multiply(
                                            DEPOSIT_RATE
                                    );

                    case BUSINESS_PROFIT ->
                            request.business()
                                    .multiply(
                                            BUSINESS_RATE
                                    );
                };

        return money(
                delta
        );
    }

    // =====================================================
    // Money normalization
    // =====================================================

    private BigDecimal money(
            BigDecimal value
    ) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    // =====================================================
    // Mapping
    // =====================================================

    private MainSimulationRunResponse toResponse(
            MainSimulationRun run
    ) {

        return new MainSimulationRunResponse(
                run.getId(),

                run.getStartingAmount(),

                run.getCashAmount(),
                run.getSavingAmount(),
                run.getDepositAmount(),
                run.getBondAmount(),
                run.getInvestmentAmount(),
                run.getSpendingAmount(),
                run.getBusinessAmount(),

                run.getPortfolioValueBeforeEvent(),

                run.getEventType(),
                run.getEventType().getTitle(),
                run.getEventType().getDescription(),

                run.getEventDelta(),
                run.getEndingAmount(),
                run.getNetChange(),

                run.getSavingScore(),
                run.getBudgetingScore(),
                run.getRiskManagementScore(),
                run.getInvestmentScore(),
                run.getDecisionMakingScore(),

                run.getXpEarned(),

                run.getCreatedAt()
        );
    }
}