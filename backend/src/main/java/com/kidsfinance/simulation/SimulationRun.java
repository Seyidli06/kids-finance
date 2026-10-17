package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "simulation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_simulation_runs_child"
            )
    )
    private ChildProfile child;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "scenario_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_simulation_runs_scenario"
            )
    )
    private SimulationScenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private SimulationRunStatus status;

    @Column(
            name = "started_at",
            nullable = false
    )
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        if (status == null) {
            status = SimulationRunStatus.IN_PROGRESS;
        }

        if (startedAt == null) {
            startedAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void complete() {
        status = SimulationRunStatus.COMPLETED;
        completedAt = OffsetDateTime.now();
    }
}