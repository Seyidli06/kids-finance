package com.kidsfinance.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationRunRepository
        extends JpaRepository<SimulationRun, Long> {

    boolean existsByChild_Id(
            Long childProfileId
    );

    List<SimulationRun>
    findAllByChild_IdOrderByCreatedAtDescIdDesc(
            Long childProfileId
    );
}