package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimulationScenarioRepository
        extends JpaRepository<SimulationScenario, Long> {

    Optional<SimulationScenario> findByCode(String code);

    Optional<SimulationScenario> findByCodeAndActiveTrue(String code);

    List<SimulationScenario> findAllByActiveTrueOrderByDisplayOrderAsc();
}