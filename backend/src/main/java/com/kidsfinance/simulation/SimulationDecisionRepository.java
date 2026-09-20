package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationDecisionRepository
        extends JpaRepository<SimulationDecision, Long> {

    boolean existsByChild_IdAndScenarioCode(
            Long childProfileId,
            String scenarioCode
    );
}