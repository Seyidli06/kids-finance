package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ConflictException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private static final String SAVE_OR_SPEND =
            "SAVE_OR_SPEND_01";

    private static final String PLAN_PURCHASE =
            "PLAN_PURCHASE_02";

    private static final List<SimulationScenarioResponse> SCENARIOS =
            List.of(
                    new SimulationScenarioResponse(
                            SAVE_OR_SPEND,
                            "İndi alım, yoxsa pulumu saxlayım?",
                            "150 AZN-lik qulaqlıq görmüsən. "
                                    + "Onu indi almaq və ya pulunu saxlamaq arasında seçim et.",
                            List.of(
                                    new SimulationOptionResponse(
                                            "BUY_HEADPHONES",
                                            "Qulaqlığı al",
                                            "İndi 150 AZN xərcləyərək qulaqlığı alırsan.",
                                            new BigDecimal("150.00")
                                    ),
                                    new SimulationOptionResponse(
                                            "KEEP_SAVINGS",
                                            "Pulunu saxla",
                                            "Qulaqlığı indi almırsan və pulunu "
                                                    + "gələcək məqsədin üçün saxlayırsan.",
                                            new BigDecimal("0.00")
                                    )
                            )
                    ),

                    new SimulationScenarioResponse(
                            PLAN_PURCHASE,
                            "Bu gün hansını alım?",
                            "Məktəb çantan köhnəlib. Yeni çanta 80 AZN, "
                                    + "bəyəndiyin oyun aksesuarı isə 120 AZN-dir. "
                                    + "Bu gün yalnız birini almağı seçirsən.",
                            List.of(
                                    new SimulationOptionResponse(
                                            "BUY_SCHOOL_BAG",
                                            "Məktəb çantasını al",
                                            "80 AZN xərcləyərək yeni məktəb çantası alırsan.",
                                            new BigDecimal("80.00")
                                    ),
                                    new SimulationOptionResponse(
                                            "BUY_GAME_ACCESSORY",
                                            "Oyun aksesuarını al",
                                            "120 AZN xərcləyərək oyun aksesuarı alırsan.",
                                            new BigDecimal("120.00")
                                    )
                            )
                    )
            );

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ChildProfileRepository childProfileRepository;
    private final SimulationDecisionRepository simulationDecisionRepository;
    private final WalletService walletService;
    private final ScoringService scoringService;

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(parentUserId, childProfileId);

        return SCENARIOS;
    }

    @Transactional
    public SimulationDecisionResponse makeDecision(
            Long parentUserId,
            Long childProfileId,
            String scenarioCode,
            SimulationDecisionRequest request
    ) {

        verifyChildBelongsToParent(parentUserId, childProfileId);

        // Ssenarini yalnız backend-də müəyyən edilmiş siyahıdan tapırıq.
        SimulationScenarioResponse scenario = SCENARIOS
                .stream()
                .filter(item -> item.code().equals(scenarioCode))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Simulation scenario not found: " + scenarioCode
                        )
                );

        // Seçim mütləq həmin ssenariyə aid olmalıdır.
        SimulationOptionResponse selectedOption = scenario.options()
                .stream()
                .filter(option -> option.code().equals(request.optionCode()))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid simulation option"
                        )
                );

        ChildProfile child = childProfileRepository
                .findByIdForUpdate(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child not found for authenticated parent"
                        )
                );

        boolean alreadyDecided = simulationDecisionRepository
                .existsByChild_IdAndScenarioCode(
                        childProfileId,
                        scenarioCode
                );

        if (alreadyDecided) {
            throw new ConflictException(
                    "Child has already completed this scenario"
            );
        }

        // Məbləği request-dən deyil, backend-dəki seçimdən götürürük.
        BigDecimal amountSpent = selectedOption.cost();
        Long walletTransactionId = null;

        // Yalnız müsbət məbləğli seçim wallet-dən pul çıxır.
        if (amountSpent.signum() > 0) {

            WalletTransactionResponse walletTransaction =
                    walletService.debit(
                            parentUserId,
                            childProfileId,
                            amountSpent,
                            "Simulation " + scenarioCode
                                    + ": " + selectedOption.code()
                    );

            walletTransactionId = walletTransaction.id();
        }

        int xpEarned =
                scoringService.awardScenarioCompletionXp(child);

        SimulationDecision decision = SimulationDecision.builder()
                .child(child)
                .scenarioCode(scenarioCode)
                .optionCode(selectedOption.code())
                .amountSpent(amountSpent)
                .walletTransactionId(walletTransactionId)
                .xpEarned(xpEarned)
                .build();

        SimulationDecision savedDecision =
                simulationDecisionRepository.save(decision);

        return new SimulationDecisionResponse(
                savedDecision.getId(),
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

        verifyChildBelongsToParent(parentUserId, childProfileId);

        return simulationDecisionRepository
                .findAllByChild_IdOrderByCreatedAtDescIdDesc(childProfileId)
                .stream()
                .map(decision -> new SimulationDecisionResponse(
                        decision.getId(),
                        decision.getScenarioCode(),
                        decision.getOptionCode(),
                        decision.getAmountSpent(),
                        decision.getWalletTransactionId(),
                        decision.getXpEarned(),
                        decision.getCreatedAt()
                ))
                .toList();
    }

    private void verifyChildBelongsToParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent = parentChildLinkRepository
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
}