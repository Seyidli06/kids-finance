package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationDecisionRepository
        extends JpaRepository<SimulationDecision, Long> {

    boolean existsByChild_IdAndScenarioCode(
            Long childProfileId,
            String scenarioCode
    );

    List<SimulationDecision> findAllByChild_IdOrderByCreatedAtDescIdDesc(
            Long childProfileId
    );
}