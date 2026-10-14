package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimulationOptionRepository
        extends JpaRepository<SimulationOption, Long> {

    List<SimulationOption>
    findAllByScenario_IdAndActiveTrueOrderByDisplayOrderAsc(
            Long scenarioId
    );

    Optional<SimulationOption>
    findByScenario_IdAndCodeAndActiveTrue(
            Long scenarioId,
            String code
    );
}