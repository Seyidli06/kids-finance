package com.kidsfinance.simulation.main;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "main_simulation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainSimulationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_main_simulation_runs_child"
            )
    )
    private ChildProfile child;

    @Column(
            name = "starting_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal startingAmount;

    @Column(name = "cash_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashAmount;

    @Column(name = "saving_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal savingAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "bond_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal bondAmount;

    @Column(name = "investment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal investmentAmount;

    @Column(name = "spending_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal spendingAmount;

    @Column(name = "business_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal businessAmount;

    @Column(
            name = "portfolio_value_before_event",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal portfolioValueBeforeEvent;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 40
    )
    private MainSimulationEventType eventType;

    @Column(
            name = "event_delta",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal eventDelta;

    @Column(
            name = "ending_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal endingAmount;

    @Column(
            name = "net_change",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal netChange;

    @Column(
            name = "saving_score",
            nullable = false
    )
    private Integer savingScore;

    @Column(
            name = "budgeting_score",
            nullable = false
    )
    private Integer budgetingScore;

    @Column(
            name = "risk_management_score",
            nullable = false
    )
    private Integer riskManagementScore;

    @Column(
            name = "investment_score",
            nullable = false
    )
    private Integer investmentScore;

    @Column(
            name = "decision_making_score",
            nullable = false
    )
    private Integer decisionMakingScore;

    @Column(
            name = "xp_earned",
            nullable = false
    )


    private Integer xpEarned;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}