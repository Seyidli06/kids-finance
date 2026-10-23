package com.kidsfinance.simulation.main;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MainSimulationRunRepository
        extends JpaRepository<MainSimulationRun, Long> {

    boolean existsByChild_Id(
            Long childProfileId
    );

    List<MainSimulationRun>
    findAllByChild_IdOrderByCreatedAtDescIdDesc(
            Long childProfileId
    );
}