package com.kidsfinance.simulation;

import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.simulation.dto.response.SimulationOptionResponse;
import com.kidsfinance.simulation.dto.response.SimulationScenarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final ParentChildLinkRepository parentChildLinkRepository;

    @Transactional(readOnly = true)
    public List<SimulationScenarioResponse> getScenariosForParent(
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

        SimulationOptionResponse buyHeadphones =
                new SimulationOptionResponse(
                        "BUY_HEADPHONES",
                        "Qulaqlığı al",
                        "İndi 150 AZN xərcləyərək qulaqlığı alırsan.",
                        new BigDecimal("150.00")
                );

        SimulationOptionResponse keepSavings =
                new SimulationOptionResponse(
                        "KEEP_SAVINGS",
                        "Pulunu saxla",
                        "Qulaqlığı indi almırsan və pulunu gələcək məqsədin üçün saxlayırsan.",
                        new BigDecimal("0.00")
                );

        SimulationScenarioResponse scenario =
                new SimulationScenarioResponse(
                        "SAVE_OR_SPEND_01",
                        "İndi alım, yoxsa pulumu saxlayım?",
                        "150 AZN-lik qulaqlıq görmüsən. " +
                                "Onu indi almaq və ya pulunu saxlamaq arasında seçim et.",
                        List.of(
                                buyHeadphones,
                                keepSavings
                        )
                );

        return List.of(scenario);
    }
}