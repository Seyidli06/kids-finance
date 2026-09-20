package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ConflictException;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
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

    private static final String SCENARIO_CODE =
            "SAVE_OR_SPEND_01";

    private static final String BUY_HEADPHONES =
            "BUY_HEADPHONES";

    private static final String KEEP_SAVINGS =
            "KEEP_SAVINGS";

    private static final BigDecimal HEADPHONES_PRICE =
            new BigDecimal("150.00");

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ChildProfileRepository childProfileRepository;
    private final SimulationDecisionRepository simulationDecisionRepository;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(parentUserId, childProfileId);

        SimulationOptionResponse buyHeadphones =
                new SimulationOptionResponse(
                        BUY_HEADPHONES,
                        "Qulaqlığı al",
                        "İndi 150 AZN xərcləyərək qulaqlığı alırsan.",
                        HEADPHONES_PRICE
                );

        SimulationOptionResponse keepSavings =
                new SimulationOptionResponse(
                        KEEP_SAVINGS,
                        "Pulunu saxla",
                        "Qulaqlığı indi almırsan və pulunu gələcək məqsədin üçün saxlayırsan.",
                        new BigDecimal("0.00")
                );

        SimulationScenarioResponse scenario =
                new SimulationScenarioResponse(
                        SCENARIO_CODE,
                        "İndi alım, yoxsa pulumu saxlayım?",
                        "150 AZN-lik qulaqlıq görmüsən. "
                                + "Onu indi almaq və ya pulunu saxlamaq arasında seçim et.",
                        List.of(buyHeadphones, keepSavings)
                );

        return List.of(scenario);
    }

    @Transactional
    public SimulationDecisionResponse makeDecision(
            Long parentUserId,
            Long childProfileId,
            String scenarioCode,
            SimulationDecisionRequest request
    ) {

        verifyChildBelongsToParent(parentUserId, childProfileId);

        if (!SCENARIO_CODE.equals(scenarioCode)) {
            throw new ResourceNotFoundException(
                    "Simulation scenario not found: " + scenarioCode
            );
        }

        if (!BUY_HEADPHONES.equals(request.optionCode())
                && !KEEP_SAVINGS.equals(request.optionCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid simulation option"
            );
        }

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

        BigDecimal amountSpent = new BigDecimal("0.00");
        Long walletTransactionId = null;

        if (BUY_HEADPHONES.equals(request.optionCode())) {

            WalletTransactionResponse walletTransaction =
                    walletService.debit(
                            parentUserId,
                            childProfileId,
                            HEADPHONES_PRICE,
                            "Simulation: bought headphones"
                    );

            amountSpent = HEADPHONES_PRICE;
            walletTransactionId = walletTransaction.id();
        }

        SimulationDecision decision = SimulationDecision.builder()
                .child(child)
                .scenarioCode(scenarioCode)
                .optionCode(request.optionCode())
                .amountSpent(amountSpent)
                .walletTransactionId(walletTransactionId)
                .build();

        SimulationDecision savedDecision =
                simulationDecisionRepository.save(decision);

        return new SimulationDecisionResponse(
                savedDecision.getId(),
                savedDecision.getScenarioCode(),
                savedDecision.getOptionCode(),
                savedDecision.getAmountSpent(),
                savedDecision.getWalletTransactionId(),
                savedDecision.getCreatedAt()
        );
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