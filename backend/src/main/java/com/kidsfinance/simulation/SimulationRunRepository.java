package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationRunRepository
        extends JpaRepository<SimulationRun, Long> {

    List<SimulationRun>
    findAllByChild_IdOrderByStartedAtDescIdDesc(
            Long childProfileId
    );
}